package com.example.ap2.MapScreenComposeables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ap2.MarkerDto
import com.example.ap2.supabase // Deinen globalen Client importieren
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
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
    val userPosition = Position(latitude = 51.023215, longitude = 7.56198)

    fun startPlacingMode() {
        currentMode = MapMode.PLACING_MARKER
    }

    fun handleMapClick(pos: Position) {
        if (currentMode == MapMode.PLACING_MARKER || currentMode == MapMode.CONFIRMING) {
            temporaryPosition = pos
            currentMode = MapMode.CONFIRMING
        }
    }

    // NEU: Nimmt die Beschreibung aus der UI entgegen und speichert in Supabase
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
                // Lade die Liste neu, damit der neue Marker inklusive ID (für Update) vorhanden ist
                fetchMarkers()
            } catch (e: Exception) {
                println("DEBUG: FEHLER beim RPC: ${e.message}")
            } finally {
                resetToDefault()
            }
        }
    }

    //Funktion zum Verändern der Inhalte des Markers
    fun updateMarker(marker: MarkerDto) {
        viewModelScope.launch {
            try {
                if (marker.id == null) return@launch

                supabase.postgrest["markers"]
                    .update({
                        set("description", marker.description)
                        set("image_url", marker.image_url)
                    }) {
                        filter { eq("id", marker.id) }
                    }
                println("DEBUG: Marker ${marker.id} erfolgreich aktualisiert!")
            } catch (e: Exception) {
                println("DEBUG: Fehler beim Update: ${e.message}")
            }
        }
    }

    //Funktion um bestehende Marker in der Datenbank zu laden
    val markerList = mutableStateListOf<MarkerDto>()

    fun fetchMarkers() {
        viewModelScope.launch {
            try {
                // WICHTIG: Hier muss .rpc stehen, nicht .select()!
                val response = supabase.postgrest.rpc("get_markers_with_coords")
                    .decodeList<MarkerDto>()

                markerList.clear()
                markerList.addAll(response)

                response.forEach { println("DEBUG: Marker nach RPC: Lat=${it.lat}, Lon=${it.lon}") }
            } catch (e: Exception) {
                println("DEBUG: Fehler: ${e.message}")
            }
        }
    }

    //Funktion um den Aktuell gesetzten Marker auszuwählen
    var selectedMarker by mutableStateOf<MarkerDto?>(null)

    fun selectMarker(marker: MarkerDto) {
        selectedMarker = marker
    }

    fun cancelPlacing() {
        resetToDefault()
    }

    private fun resetToDefault() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }
}