package com.example.ap2.mapScreenComposables

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data_models.AppDatabase
import com.example.ap2.data_models.MapMarkerUiState
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.data_models.ProfileDto
import com.example.ap2.repositories.MapRepository
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Position

enum class MapMode {
    DEFAULT,
    PLACING_MARKER,
    CONFIRMING
}

class MapViewModel : ViewModel() {

    private lateinit var repository: MapRepository

    // --- STATE VARIABLEN ---
    var userPosition by mutableStateOf(Position(longitude = 7.6261, latitude = 51.2180))
    var currentMode by mutableStateOf(MapMode.DEFAULT)
    var selectedMarker by mutableStateOf<MarkerDto?>(null)
    var temporaryPosition by mutableStateOf<Position?>(null)

    private var lastFogPosition: Position? = null
    private val fogUpdateDistance = 25.0

    var fogGeoJson by mutableStateOf<String?>(null)
        private set

    // --- DATEN-LISTEN ---
    val markerList = mutableStateListOf<MarkerDto>()
    val mapMarkers = mutableStateListOf<MapMarkerUiState>()

    var centerOnUserTrigger by mutableIntStateOf(0)
        private set

    var isFollowingUser by mutableStateOf(false)
        private set

    // Gesamt Distanz zurückgelegt
    var totalDistance by mutableStateOf(0f)
    private var lastStepPosition: Position? = null

    // Durchschnittliche Schrittlänge in Meter
    private val stepLength = 0.75f

    val stepsFromDistance: Int
        get() = (totalDistance / stepLength).toInt()

    // für die Blickrichtung
    var userBearing by mutableStateOf(0f)

    // Profil aus Datenbank laden
    var currentUserProfile by mutableStateOf<ProfileDto?>(null)
        private set

    val ownMarkersCount by derivedStateOf {
        markerList.count { it.user_id == supabase.auth.currentUserOrNull()?.id }
    }

    val markersSharedWithMeCount by derivedStateOf {
        val myId = supabase.auth.currentUserOrNull()?.id
        markerList.count { it.user_id != myId && it.user_id.isNotEmpty()}
    }

    private var isFogUpdateInProgress = false

    var isDarkMode by mutableStateOf(false) // Standardmäßig Light Mode
        private set

    val mapStyle: String
        get() = if (isDarkMode) {
            "https://tiles.openfreemap.org/styles/dark"
        } else {
            "https://tiles.openfreemap.org/styles/liberty"
        }


    // --- REPOSITORY INITIALISIERUNG & FLOW OBSERVATION ---

    fun initRepository(context: Context) {
        if (!::repository.isInitialized) {
            repository = MapRepository(context.applicationContext)
            observeLocalData()
        }
    }

    private fun observeLocalData() {
        val currentUser = supabase.auth.currentUserOrNull()

        // 1. Alle für diesen Nutzer synchronisierten Marker (eigene + geteilte) beobachten
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMarkersFlow().collect { markers ->
                withContext(Dispatchers.Main) {
                    markerList.clear()
                    markerList.addAll(markers)
                }
            }
        }

        // 2. Fog GeoJSON aus Room beobachten
        if (currentUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getFogFlow(currentUser.id).collect { geoJson ->
                    withContext(Dispatchers.Main) {
                        if (geoJson != null) {
                            fogGeoJson = geoJson
                        }
                    }
                }
            }
        }

        loadFog()
        repository.triggerBackgroundSync()
    }

    // --- MAP STEUERUNG ---

    fun centerOnUserLocation() {
        selectedMarker = null
        isFollowingUser = true
        centerOnUserTrigger++
    }

    fun stopFollowingUser() {
        isFollowingUser = false
    }

    fun startPlacingMode() {
        currentMode = MapMode.PLACING_MARKER
    }

    fun handleMapClick(position: Position) {
        if (currentMode == MapMode.PLACING_MARKER) {
            temporaryPosition = position
            currentMode = MapMode.CONFIRMING
        }
    }

    fun cancelPlacing() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }

    fun resetMode() {
        cancelPlacing()
    }

    // --- OFFLINE-FIRST MARKER OPERATIONEN ---

    fun confirmMarker(
        context: Context,
        description: String = "Neuer Marker",
        color: String = "#2196F3",
        imageBytes: ByteArray? = null
    ) {
        initRepository(context)

        val pos = temporaryPosition ?: return
        val currentUser = supabase.auth.currentUserOrNull() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Generiere EINE feste ID für diesen neuen Marker
                val newMarkerId = java.util.UUID.randomUUID().toString()

                repository.saveMarkerLocally(
                    userId = currentUser.id,
                    lat = pos.latitude,
                    lon = pos.longitude,
                    desc = description,
                    color = color,
                    imageBytes = imageBytes,
                    markerId = newMarkerId //Feste ID übergeben
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Marker gespeichert!", Toast.LENGTH_SHORT).show()
                    cancelPlacing()
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Speichern: ${e.message}", e)
            }
        }
    }

    fun selectMarker(marker: MarkerDto?) {
        selectedMarker = marker
    }

    fun deleteMarker(id: String) {
        val currentUser = supabase.auth.currentUserOrNull() ?: return
        val targetMarker = markerList.find { it.id == id }

        // Nur der Eigentümer darf löschen
        if (targetMarker?.user_id != currentUser.id) return

        selectedMarker = null
        viewModelScope.launch(Dispatchers.IO) {
            if (::repository.isInitialized) {
                repository.deleteMarker(id)
            }
        }
    }

    fun updateUserPosition(position: Position) {
        if (lastStepPosition == null) {
            lastStepPosition = position
            userPosition = position
            checkFogUpdate(position)
        }

        // 2. Bewegung berechnen
        val previousPosition = lastStepPosition!!
        userPosition = position
        lastStepPosition = position // Aktuelle Position für das nächste Mal merken

        val distanceMoved = distanceBetween(previousPosition, position)

        // 3. Schritte zählen (Filter: Nur Bewegungen > 2 Meter zählen gegen GPS-Rauschen)
        if (distanceMoved > 2.0f) {
            totalDistance += distanceMoved
        }

        // 4. Fog-Logik anstoßen
        checkFogUpdate(position)
    }

    private fun distanceBetween(first: Position, second: Position): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            first.latitude,
            first.longitude,
            second.latitude,
            second.longitude,
            result
        )
        return result[0]
    }

    private fun checkFogUpdate(position: Position) {
        if (isFogUpdateInProgress) return

        val lastPosition = lastFogPosition

        if (lastPosition == null) {
            lastFogPosition = position
            addFogPoint(position)
            return
        }

        val distance = distanceBetween(lastPosition, position)

        if (distance >= fogUpdateDistance) {
            lastFogPosition = position
            addFogPoint(position)
        }
    }

    private fun addFogPoint(position: Position) {
        isFogUpdateInProgress = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Lokal puffern für Offline-Sync
                repository.recordFogPointLocally(position.latitude, position.longitude)

                // 2. RPC direkt versuchen, um Nebel auf der Karte sofort aufzudecken
                supabase.postgrest.rpc(
                    "add_fog_point",
                    buildJsonObject {
                        put("new_lat", position.latitude)
                        put("new_lon", position.longitude)
                    }
                )

                // 3. GeoJSON auf der Karte live aktualisieren
                loadFog()
                Log.d("MapViewModel", "Fog live aktualisiert: ${position.latitude}, ${position.longitude}")
            } catch (e: Exception) {
                Log.e("MapViewModel", "Offline: Fog-Punkt lokal gepuffert")
            }
        }
    }

    /**
     * Lädt das aktuelle Fog-GeoJSON direkt aus Supabase.
     */
    fun loadFog() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = supabase.auth.currentUserOrNull() ?: return@launch


                val result = supabase.postgrest.rpc("get_user_fog")

                withContext(Dispatchers.Main) {
                    fogGeoJson = result.data
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Laden des Fogs: ${e.message}")
            } finally {
                isFogUpdateInProgress = false
            }
        }
    }

    suspend fun loadMarkersForMap() {
        if (::repository.isInitialized) {
            repository.triggerBackgroundSync()
        }
    }

    fun updateMarkerWithImage(
        markerId: String,
        description: String,
        color: String,
        oldImageUrl: String?,
        newImageBytes: ByteArray?
    ) {
        val currentUser = supabase.auth.currentUserOrNull() ?: return
        val currentMarker = markerList.find { it.id == markerId } ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveMarkerLocally(
                    userId = currentUser.id,
                    lat = currentMarker.lat,
                    lon = currentMarker.lon,
                    desc = description,
                    color = color,
                    imageBytes = newImageBytes,
                    existingImageUrl = oldImageUrl,
                    markerId = markerId
                )
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Aktualisieren: ${e.message}", e)
            }
        }
    }

    fun initializeFog() {
        viewModelScope.launch {
            try {
                supabase.postgrest.rpc("ensure_user_fog")
                loadFog()
            } catch (e: Exception) { /* ... */ }
        }
    }

    fun resetUserDataOnLogout(context: Context) {
        // 1. In-Memory Zustand leeren
        fogGeoJson = null
        lastFogPosition = null
        currentUserProfile = null
        selectedMarker = null
        temporaryPosition = null
        totalDistance = 0f
        markerList.clear()

        // 2. Lokale Room-Datenbank säubern
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(context.applicationContext).mapDao()
            dao.clearAllMarkers()
            dao.clearFogCache()
            dao.clearPendingFogPoints()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

    fun fetchCurrentUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            try {
                val profile = supabase.postgrest.from("profiles")
                    .select() { filter { eq("id", user.id) } }
                    .decodeSingle<ProfileDto>()
                withContext(Dispatchers.Main) { currentUserProfile = profile }
            } catch (e: Exception) { /* Log error */ }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            try {
                supabase.postgrest.from("profiles")
                    .update({ set("username", newUsername) }) { filter { eq("id", user.id) } }
                fetchCurrentUserProfile()
            } catch (e: Exception) { /* Log error */ }
        }
    }

    fun updateProfileImage(imageBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            try {
                // 1. Bild in den Supabase Storage hochladen
                // Wir nutzen die User-ID als Dateinamen, um das alte Bild zu überschreiben
                val fileName = "profile_${user.id}.jpg"
                val bucket = supabase.storage.from("avatars")

                // 'upsert = true' sorgt dafür, dass die Datei ersetzt wird, falls sie schon existiert
                bucket.upload(fileName, imageBytes, upsert = true)

                // 2. Die öffentliche URL des hochgeladenen Bildes abrufen
                val avatarUrl = bucket.publicUrl(fileName)

                // 3. Die URL in der 'profiles' Tabelle in der Datenbank speichern
                supabase.postgrest.from("profiles").update({
                    set("avatar_url", avatarUrl) // Prüfe, ob die Spalte in deiner DB 'image_url' heißt
                }) {
                    filter { eq("id", user.id) }
                }

                // 4. Das Profil neu laden, damit die UI das neue Bild sofort anzeigt
                fetchCurrentUserProfile()

                Log.d("MapViewModel", "Profilbild erfolgreich aktualisiert: $avatarUrl")
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Profilbild-Update: ${e.message}", e)
            }
        }
    }
}