package com.example.ap2.MapScreenComposeables

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // WICHTIG: Dieser Import muss da sein!
import com.example.ap2.MarkerDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Position


enum class MapMode {
    DEFAULT, PLACING_MARKER, CONFIRMING
}

class MapViewModel : ViewModel() {
    var currentMode by mutableStateOf(MapMode.DEFAULT)
        private set

    var temporaryPosition by mutableStateOf<Position?>(null)
        private set

    val savedMarkers = mutableStateListOf<Position>()
    var userPosition by mutableStateOf(Position(latitude = 51.023215, longitude = 7.56198))
        private set
    val markerList = mutableStateListOf<MarkerDto>()
    var selectedMarker by mutableStateOf<MarkerDto?>(null)
        private set

    fun startPlacingMode() {
        currentMode = MapMode.PLACING_MARKER
    }

    fun handleMapClick(pos: Position) {
        if (currentMode == MapMode.PLACING_MARKER || currentMode == MapMode.CONFIRMING) {
            temporaryPosition = pos
            currentMode = MapMode.CONFIRMING
        }
    }

    fun confirmMarker(description: String) {
        val pos = temporaryPosition ?: return
        val user = supabase.auth.currentUserOrNull()

        viewModelScope.launch {
            try {
                supabase.postgrest.rpc(
                    "create_marker",
                    parameters = buildJsonObject {
                        put("lat", pos.latitude)
                        put("lon", pos.longitude)
                        put("description", description)
                        put("color", "red")
                        put("image_url", "")
                        put("user_id", user?.id)
                    }
                )
                fetchMarkers()
            } catch (e: Exception) {
                println("DEBUG: FEHLER beim RPC: ${e.message}")
            } finally {
                resetToDefault()
            }
        }
    }

    fun deleteMarker(markerId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (markerId.isNotEmpty()) {
                    // 1. Datenbank-Löschung
                    supabase.postgrest.from("markers").delete {
                        filter { eq("id", markerId) }
                    }

                    // 2. Lokale Liste mutieren
                    markerList.removeAll { it.id == markerId }

                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun fetchMarkers() {
        Log.d("DB", "Versuche Marker zu laden...")
        try {
            val list = supabase.postgrest.from("markers")
                .select()
                .decodeList<MarkerDto>()

            Log.d("DB", "Marker geladen: ${list.size}")
            markerList.clear()
            markerList.addAll(list)
        } catch (e: Exception) {
            Log.e("DB", "FEHLER beim Laden: ${e.message}") // HIER IM LOGCAT SCHAUEN!
        }
    }

    // JETZT KORREKT INNERHALB DER KLASSE PLATZIERT:
    fun updateMarkerWithImage(id: String, newDescription: String, newColor: String, oldImageUrl: String, newImageBytes: ByteArray?) {
        viewModelScope.launch {
            try {
                var finalImageUrl = oldImageUrl

                if (newImageBytes != null) {
                    val uploadedUrl = uploadMarkerImageAndGetUrl(id, newImageBytes)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                supabase.postgrest.from("markers")
                    .update({
                        set("description", newDescription)
                        set("color", newColor)
                        set("image_url", finalImageUrl)
                    }) {
                        filter { eq("id", id) }
                    }

                println("DEBUG: Marker $id erfolgreich aktualisiert!")
                fetchMarkers()
                selectedMarker = null

            } catch (e: Exception) {
                println("DEBUG: Fehler beim kombinierten Update: ${e.message}")
            }
        }
    }

    fun selectMarker(marker: MarkerDto) {
        selectedMarker = marker
    }

    fun cancelPlacing() {
        resetToDefault()
    }

    fun updateUserPosition(newPosition: Position) {
        // Falls deine Variable im ViewModel anders heißt (z.B. userLocation),
        // passe den Namen hier entsprechend an!
        userPosition = newPosition
    }

    private fun resetToDefault() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }

    suspend fun uploadMarkerImageAndGetUrl(markerId: String, imageBytes: ByteArray): String? {
        return try {
            val storagePath = "markers/$markerId.jpg"
            supabase.storage["marker-images"].upload(storagePath, imageBytes, upsert = true)
            supabase.storage["marker-images"].publicUrl(storagePath)
        } catch (e: Exception) {
            Log.e("Storage", "Bild-Upload fehlgeschlagen: ${e.message}")
            null
        }
    }
}