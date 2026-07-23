package com.example.ap2.mapScreenComposables

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import com.example.ap2.homeScreenComposables.SmallMarkerCompose
import com.example.ap2.data_models.MarkerDto
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    camera: CameraState, // Wir nutzen durchgehend diesen Parameter!
    onMapClick: (Position) -> Unit,
    onMarkerClick: (MarkerDto) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isInitialLocationSet by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Reagiert auf Klicks auf den Standort-Button und animiert die Karte über 'camera'
    LaunchedEffect(viewModel.centerOnUserTrigger) {
        if (viewModel.centerOnUserTrigger > 0) {
            val targetCameraPosition = CameraPosition(
                target = viewModel.userPosition,
                zoom = 15.0
            )
            camera.animateTo(
                finalPosition = targetCameraPosition
            )
        }
    }

    // EINZIGE Haupt-Box für alle Layer (Karte -> Marker -> Overlays -> Button)
    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
        if (hasLocationPermission) {
            val locationProvider = rememberDefaultLocationProvider()
            val orientationProvider = rememberDefaultOrientationProvider()
            val locationState = rememberUserLocationState(locationProvider, orientationProvider)

            LaunchedEffect(locationState.location) {
                val position = locationState.location?.position?.value
                if (position != null) {
                    viewModel.updateUserPosition(position)
                    if (!isInitialLocationSet) {
                        camera.animateTo(CameraPosition(target = position, zoom = 17.0))
                        isInitialLocationSet = true
                    }
                }
            }

            LaunchedEffect(Unit) {
                viewModel.loadMarkersForMap()
            }

            // 1. --- KARTE ---
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
            ) {
                LocationPuck(idPrefix = "user", location = locationState.location, bearing = locationState.mostAccurateBearing(), cameraState = camera)
                LocationTrackingEffect(locationState = locationState) {}
            }

            val currentCameraState = camera.position
            val projection = camera.projection

            val markerOffsetX = 16.dp
            val markerOffsetY = 32.dp

            viewModel.markerList.forEach { markerDto ->
                val lat = markerDto.lat
                val lon = markerDto.lon

                val markerPos = Position(longitude = lon, latitude = lat)

                // Später für Fog of War:
                // val isDiscovered = fogOfWarManager.isLocationRevealed(markerPos)
                // if (isDiscovered) { ... }

                // Mappt die Position dynamisch bei jeder Kamerabewegung
                val screenPos = remember(currentCameraState, markerPos) {
                    projection?.screenLocationFromPosition(markerPos)
                }

                if (screenPos != null) {
                    Box(
                        modifier = Modifier.offset {
                            IntOffset(
                                (screenPos.x - markerOffsetX).roundToPx(),
                                (screenPos.y - markerOffsetY).roundToPx()
                            )
                        }
                    ) {
                        SmallMarkerCompose(
                            markerDto = markerDto,
                            onExpandRequested = {
                                viewModel.selectMarker(markerDto)
                                onMarkerClick(markerDto)
                            }
                        )
                    }
                }
            }

            // Temporärer Marker bei Bestätigung
            if (viewModel.currentMode == MapMode.CONFIRMING) {
                viewModel.temporaryPosition?.let { tempPos ->
                    val screenPos = projection?.screenLocationFromPosition(tempPos)
                    if (screenPos != null) {
                        Box(modifier = Modifier.offset {
                            IntOffset(
                                (screenPos.x - markerOffsetX).roundToPx(),
                                (screenPos.y - markerOffsetY).roundToPx()
                            )
                        }) {
                            SmallMarkerCompose(
                                markerDto = MarkerDto(id = "", user_id = "", lat = tempPos.latitude, lon = tempPos.longitude),
                                onExpandRequested = {}
                            )
                        }
                    }
                }
            }

            // 3. --- OVERLAYS & HINTS ---
            when (viewModel.currentMode) {
                MapMode.PLACING_MARKER -> PlacingModeHint(
                    onCancel = { viewModel.cancelPlacing() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                MapMode.CONFIRMING -> ConfirmMarkerOverlay(
                    onConfirm = {
                        viewModel.confirmMarker(context = context, description = "Neuer Marker")
                    },
                    onCancel = { viewModel.cancelPlacing() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
                else -> {}
            }

            // 4. --- STANDORT BUTTON (Ganz oben auf dem Stapel) ---
            FloatingActionButton(
                onClick = { viewModel.centerOnUserLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Zurück zu meiner Position"
                )
            }
        }
    }
}