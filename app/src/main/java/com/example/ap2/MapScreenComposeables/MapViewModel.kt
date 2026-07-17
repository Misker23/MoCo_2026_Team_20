package com.example.ap2.MapScreenComposeables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.maplibre.spatialk.geojson.Position

enum class MapMode {
    DEFAULT, PLACING_MARKER, CONFIRMING
}

class MapViewModel : ViewModel() {
    // UI-Zustände (States)
    var currentMode by mutableStateOf(MapMode.DEFAULT)
        private set // Nur das ViewModel darf den Zustand direkt ändern

    var temporaryPosition by mutableStateOf<Position?>(null)
        private set

    // Liste aller Marker
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

    fun confirmMarker() {
        temporaryPosition?.let { savedMarkers.add(it) }
        resetToDefault()
    }

    fun cancelPlacing() {
        resetToDefault()
    }

    private fun resetToDefault() {
        temporaryPosition = null
        currentMode = MapMode.DEFAULT
    }
}