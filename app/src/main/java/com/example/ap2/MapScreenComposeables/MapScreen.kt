package com.example.ap2.MapScreenComposeables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import com.example.ap2.HomeScreenComposables.SmallMarker

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onMapClick: (Position) -> Unit,
    onMarkerClick: () -> Unit // soll Markerwindow öffnen
) {
    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = viewModel.userPosition,
            zoom = 16.5,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        // 1. Karte rendern
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = camera,
            onMapClick = { pos, _ ->
                viewModel.handleMapClick(pos)
                onMapClick(pos)
                ClickResult.Consume
            },
            options = MapOptions(
                gestureOptions = GestureOptions(isTiltEnabled = false, isZoomEnabled = true, isRotateEnabled = true, isScrollEnabled = true),
                ornamentOptions = OrnamentOptions(isCompassEnabled = true, compassAlignment = Alignment.TopEnd, isScaleBarEnabled = true, scaleBarAlignment = Alignment.TopStart)
            )
        )

        // Kamera-State
        val currentCameraState = camera.position

        // GPS eigner Standort (gefaked)
        val userScreenPos = camera.projection?.screenLocationFromPosition(viewModel.userPosition)
        if (userScreenPos != null) {
            Box(
                modifier = Modifier
                    .offset(x = userScreenPos.x - 12.dp, y = userScreenPos.y - 12.dp) // Mittig platzieren
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)) // Blau Farbe
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.Center)
                )
            }
        }

        // gespeicherte Marker setzen
        viewModel.savedMarkers.forEach { savedPos ->
            val screenPos = camera.projection?.screenLocationFromPosition(savedPos)
            if (screenPos != null) {
                Box(modifier = Modifier.offset(x = screenPos.x - 24.dp, y = screenPos.y - 24.dp)) {
                    SmallMarker(onExpandRequested = {
                        onMarkerClick() // Öffnet das Fenster aus dem HomeScreen
                    })
                }
            }
        }

        // temp Marker setzen
        if (viewModel.currentMode == MapMode.CONFIRMING) {
            viewModel.temporaryPosition?.let { tempPos ->
                val screenPos = camera.projection?.screenLocationFromPosition(tempPos)
                if (screenPos != null) {
                    Box(modifier = Modifier.offset(x = screenPos.x - 24.dp, y = screenPos.y - 24.dp)) {
                        SmallMarker(onExpandRequested = {})
                    }
                }
            }
        }

        // Overlays nach Modus anzeigen
        when (viewModel.currentMode) {
            MapMode.PLACING_MARKER -> {
                PlacingModeHint(
                    onCancel = { viewModel.cancelPlacing() }, // Leitet den Abbruch ans ViewModel weiter
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            MapMode.CONFIRMING -> {
                ConfirmMarkerOverlay(
                    onConfirm = { viewModel.confirmMarker() }, // Leitet Bestätigung ans ViewModel weiter
                    onCancel = { viewModel.cancelPlacing() }, // Leitet Abbruch ans ViewModel weiter
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            else -> {}
        }
    }
}