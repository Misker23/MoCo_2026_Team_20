package com.example.ap2.MapScreenComposeables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

        println("DEBUG: Starte confirmMarker für User: ${user?.id}")

        viewModelScope.launch {
            try {
                println("DEBUG: Sende RPC an Supabase...")

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

                println("DEBUG: RPC erfolgreich gesendet!")
                savedMarkers.add(pos)
            } catch (e: Exception) {
                println("DEBUG: FEHLER beim RPC: ${e.message}")
            } finally {
                resetToDefault()
            }
        }
    }

    fun cancelPlacing() {
        resetToDefault()
    }

    private fun resetToDefault() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }
}