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
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Position
import java.util.UUID

enum class MapMode {
    DEFAULT,
    PLACING_MARKER,
    CONFIRMING
}

class MapViewModel : ViewModel() {

    var userPosition by mutableStateOf(Position(longitude = 7.6261, latitude = 51.2180))
    var currentMode by mutableStateOf(MapMode.DEFAULT)
    var selectedMarker by mutableStateOf<MarkerDto?>(null)
    var temporaryPosition by mutableStateOf<Position?>(null)

    val markerList = mutableStateListOf<MarkerDto>()
    val mapMarkers = mutableStateListOf<MapMarkerUiState>()

    var centerOnUserTrigger by mutableIntStateOf(0)
        private set

    fun centerOnUserLocation() {
        selectedMarker = null // Schließt optional das Marker-Detail-Sheet
        centerOnUserTrigger++  // Löst ein Event in der UI aus
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

    // 1. NEU: imageBytes als optionaler Parameter ergänzt
    fun confirmMarker(
        context: Context,
        description: String = "Neuer Marker",
        color: String = "#2196F3",
        imageBytes: ByteArray? = null
    ) {
        val pos = temporaryPosition ?: return

        viewModelScope.launch {
            val currentUser = supabase.auth.currentUserOrNull() ?: return@launch

            try {
                var uploadedImageUrl: String? = null

                // Bild hochladen, falls ein Bild ausgewählt wurde
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    val fileName = "marker_${UUID.randomUUID()}.jpg"
                    val bucket = supabase.storage.from("marker-images")

                    // Hier mit Komma statt geschweiften Klammern:
                    bucket.upload(fileName, imageBytes, upsert = false)
                    uploadedImageUrl = bucket.publicUrl(fileName)
                }

                val newMarker = buildJsonObject {
                    put("user_id", currentUser.id)
                    put("lat", pos.latitude)
                    put("lon", pos.longitude)
                    put("position", "POINT(${pos.longitude} ${pos.latitude})")
                    put("description", description)
                    put("color", color)
                    if (uploadedImageUrl != null) {
                        put("image_url", uploadedImageUrl)
                    }
                }

                supabase.postgrest.from("markers").insert(newMarker)

                Toast.makeText(context, "Marker gespeichert!", Toast.LENGTH_SHORT).show()
                cancelPlacing()
                loadMarkersForMap()

            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Speichern: ${e.message}", e)
                Toast.makeText(context, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun selectMarker(marker: MarkerDto?) {
        selectedMarker = marker
    }

    fun updateUserPosition(position: Position) {
        userPosition = position
    }

    suspend fun loadMarkersForMap() {
        val currentUser = supabase.auth.currentUserOrNull() ?: run {
            Log.e("MapViewModel", "❌ CANCELLED: Kein User in Supabase Auth angemeldet!")
            return
        }

        try {
            Log.d("MapViewModel", "🔄 Starte Laden der Marker für User: ${currentUser.id}...")

            val rawMarkers = supabase.postgrest.from("markers")
                .select()
                .decodeList<MarkerDto>()

            Log.d("MapViewModel", "SUCCESS! ${rawMarkers.size} Marker geladen.")

            val processedMarkers = rawMarkers.map { marker ->
                MapMarkerUiState(
                    id = marker.id ?: "",
                    creatorId = marker.user_id,
                    lat = marker.lat ?: 0.0,
                    lon = marker.lon ?: 0.0,
                    description = marker.description ?: "",
                    imageUrl = marker.image_url,
                    displayColor = marker.color ?: "#2196F3",
                    isOwnMarker = marker.user_id == currentUser.id
                )
            }

            markerList.clear()
            markerList.addAll(rawMarkers)

            mapMarkers.clear()
            mapMarkers.addAll(processedMarkers)

        } catch (e: Exception) {
            Log.e("MapViewModel", "💥 EXCEPTION BEIM LADEN DER MARKER: ${e.message}", e)
        }
    }

    fun updateMarkerWithImage(
        markerId: String,
        description: String,
        color: String,
        oldImageUrl: String?,
        newImageBytes: ByteArray?
    ) {
        viewModelScope.launch {
            try {
                var finalImageUrl = oldImageUrl

                if (newImageBytes != null && newImageBytes.isNotEmpty()) {
                    val fileName = "marker_${UUID.randomUUID()}.jpg"
                    val bucket = supabase.storage.from("marker-images")

                    // Auch hier mit Komma:
                    bucket.upload(fileName, newImageBytes, upsert = true)
                    finalImageUrl = bucket.publicUrl(fileName)
                }

                // 2. KORREKTUR: supabase.postgrest.from statt supabase.from
                supabase.postgrest.from("markers").update({
                    set("description", description)
                    set("color", color)
                    set("image_url", finalImageUrl)
                }) {
                    filter {
                        eq("id", markerId)
                    }
                }

                loadMarkersForMap()

            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Aktualisieren des Markers", e)
            }
        }
    }

    fun deleteMarker(id: String) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("markers").delete {
                    filter { eq("id", id) }
                }
                selectedMarker = null
                loadMarkersForMap()
            } catch (e: Exception) {
                Log.e("MapViewModel", "Fehler beim Löschen: ${e.message}")
            }
        }
    }
}