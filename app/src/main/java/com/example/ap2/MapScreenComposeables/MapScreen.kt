package com.example.ap2.MapScreenComposeables

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState

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




    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInitialLocationSet by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Re-check permission when app returns to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        if (hasLocationPermission) {
            val locationProvider = rememberDefaultLocationProvider()
            val orientationProvider = rememberDefaultOrientationProvider()

            val locationState =
                rememberUserLocationState(
                    locationProvider,
                    orientationProvider
                )

            LaunchedEffect(locationState.location) {
                val position = locationState.location?.position?.value
                if (position != null && !isInitialLocationSet) {
                    camera.animateTo(
                        CameraPosition(
                            target = position,
                            zoom = 17.0
                        )
                    )
                    isInitialLocationSet = true
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.fetchMarkers()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Karte rendern
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
                    gestureOptions = GestureOptions(
                        isTiltEnabled = false,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true
                    ),
                    ornamentOptions = OrnamentOptions(
                        isCompassEnabled = true,
                        compassAlignment = Alignment.TopEnd,
                        isScaleBarEnabled = true,
                        scaleBarAlignment = Alignment.TopStart
                    )
                )
            ) {

                if (hasLocationPermission) {
                    val locationProvider = rememberDefaultLocationProvider()
                    val orientationProvider = rememberDefaultOrientationProvider()

                    val locationState =
                        rememberUserLocationState(
                            locationProvider,
                            orientationProvider
                        )

                    LocationPuck(
                        idPrefix = "user",
                        location = locationState.location,
                        bearing = locationState.mostAccurateBearing(),
                        cameraState = camera
                    )

                    LocationTrackingEffect(
                        locationState = locationState
                    ) {
                        // Ongoing location tracking logic can go here if needed
                    }
                }

            }
        }

        // Kamera-State
        val currentCameraState = camera.position

        // gespeicherte Marker setzen
        viewModel.markerList.forEach { markerDto ->
            // Wenn dein DTO 'lat' und 'lon' hat:
            val markerPos = Position(markerDto.lon, markerDto.lat)

            val screenPos = camera.projection?.screenLocationFromPosition(markerPos)
            if (screenPos != null) {
                Box(modifier = Modifier.offset(x = screenPos.x - 24.dp, y = screenPos.y - 24.dp)) {
                    SmallMarker(onExpandRequested = {
                        viewModel.selectMarker(markerDto) // Marker fürs Update wählen
                        onMarkerClick() // Dialog öffnen
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
                    onConfirm = { viewModel.confirmMarker("Ein neu platzierter Marker!") }, // Leitet Bestätigung ans ViewModel weiter
                    onCancel = { viewModel.cancelPlacing() }, // Leitet Abbruch ans ViewModel weiter
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            else -> {}
        }
    }
}