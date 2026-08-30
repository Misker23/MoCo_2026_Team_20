package com.example.ap2.mapScreenComposables

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.data_models.MapMarkerUiState
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.repositories.MapRepository
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
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

    // --- REPOSITORY INITIALISIERUNG & FLOW OBSERVATION ---

    fun initRepository(context: Context) {
        if (!::repository.isInitialized) {
            repository = MapRepository(context.applicationContext)
            observeLocalData()
        }
    }

    private fun observeLocalData() {
        val currentUser = supabase.auth.currentUserOrNull()

        // 1. Marker aus Room beobachten
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMarkersFlow().collect { markers ->
                withContext(Dispatchers.Main) {
                    markerList.clear()
                    markerList.addAll(markers)
                }
            }
        }

        // 2. Fog GeoJSON aus Room beobachten (falls User angemeldet)
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

        // 3. Nebel initial direkt laden & Sync anstoßen
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
        selectedMarker = null
        viewModelScope.launch(Dispatchers.IO) {
            if (::repository.isInitialized) {
                repository.deleteMarker(id)
            }
        }
    }

    fun updateUserPosition(position: Position) {
        userPosition = position
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

                supabase.postgrest.rpc("ensure_user_fog")
                val result = supabase.postgrest.rpc("get_user_fog")

                withContext(Dispatchers.Main) {
                    fogGeoJson = result.data
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Laden des Fogs: ${e.message}")
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
}