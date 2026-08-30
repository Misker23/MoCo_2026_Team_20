package com.example.ap2.homeScreenComposables

import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ap2.R
import com.example.ap2.mapScreenComposables.MapMode
import com.example.ap2.mapScreenComposables.MapScreen
import com.example.ap2.mapScreenComposables.MapViewModel
import com.example.ap2.sensor_repositories.MotionRepository
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

/**
 * Detail- und Bearbeitungsdialog für einen ausgewählten Marker.
 * Bietet die Möglichkeit, Beschreibung, Marker-Farbe sowie ein Bild hochzuladen oder den Marker zu löschen.
 *
 * @param bottomPadding Abstand nach unten (z. B. für die Navigation-Bar).
 * @param markerDto Das zu bearbeitende Marker-Objekt.
 * @param onDismiss Callback zum Schließen des Popups.
 * @param onSave Callback zum Speichern mit geänderter Beschreibung, Farbe und optionalen Bild-Bytes.
 * @param onDelete Callback zum endgültigen Löschen des Markers aus der Datenbank.
 */
@Composable
fun HomeScreen(
    viewModel: MapViewModel,
    onNavigateToFriends: () -> Unit,
    onLogout: () -> Unit
) {
    var isSneakPeekVisible by remember { mutableStateOf(false) }

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = viewModel.userPosition,
            zoom = 16.5,
        )
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val windowManager =
        remember { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    // 1. Initialisierung Sensoren & Daten-Upload
    val motionRepo = remember { MotionRepository(context) }
    var compassDegree by remember { mutableFloatStateOf(0f) }

    // 2. Initialer Start: Marker laden & Sensoren abonnieren
    LaunchedEffect(Unit) {
        // Marker aus Supabase laden
        viewModel.loadMarkersForMap()
        viewModel.fetchCurrentUserProfile()

//        // Schritte sammeln
//        launch {
//            motionRepo.getStepCountUpdates().collect { newSteps ->
//                steps = newSteps
//            }
//        }
//
        // Kompass sammeln
        launch {
            motionRepo.getRotationUpdates().collect { azimuth ->
                val rotation = windowManager.defaultDisplay.rotation
                val rotationDegrees = when (rotation) {
                    android.view.Surface.ROTATION_90 -> 90f
                    android.view.Surface.ROTATION_180 -> 180f
                    android.view.Surface.ROTATION_270 -> 270f
                    else -> 0f
                }
                val correctedBearing = (-(azimuth + rotationDegrees) + 360) % 360
                compassDegree = correctedBearing
                viewModel.userBearing = correctedBearing
            }
        }
    }

    var markerPosition by remember { mutableStateOf<Position?>(null) }
    var isMarkerWindowVisable by remember { mutableStateOf(false) }
    var isPoiWindowVisable by remember { mutableStateOf(false) }
    var isSettingWindowVisable by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = .5f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    POIButton(
                        onClick = {isPoiWindowVisable = true},
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.15f))
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FriendsButton(
                        onClick = {onNavigateToFriends()},
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.15f))
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SettingButton(
                        onClick = {isSettingWindowVisable = true},
                        modifier = Modifier
                    )
                }

            }
        },

        floatingActionButton = {
            if (viewModel.currentMode == MapMode.DEFAULT) {
                FloatingActionButton(
                    onClick = { viewModel.startPlacingMode() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_location_alt_24),
                        contentDescription = "Marker hinzufügen"
                    )
                }
            }
        }
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            // MAP VIEW
            MapScreen(
                viewModel = viewModel,
                camera = camera,
                onMapClick = { pos ->
                    markerPosition = pos
                    isSneakPeekVisible = false
                    isMarkerWindowVisable = false
                },
                onMarkerClick = { clickedMarker ->
                    // Setzt den gewählten Marker im ViewModel und öffnet die Vorschau
                    viewModel.selectMarker(clickedMarker)
                    isSneakPeekVisible = true
                }
            )

            // PROFILE BUTTON
            if (viewModel.currentMode == MapMode.DEFAULT) {
                ProfileButton(
                    onLogout = onLogout,
                    modifier = Modifier,
                    viewModel = viewModel
                )
            }

            // SCHRITTE
//            if (viewModel.currentMode == MapMode.DEFAULT) {
//                Box(
//                    modifier = Modifier
//                        .padding(start = 16.dp, top = 80.dp)
//                        .size(width = 120.dp, height = 30.dp)
//                        .align(Alignment.TopStart)
//                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Schritte: ${viewModel.stepsFromDistance}",
//                        color = Color.Black,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }

            // SNEAK PEEK DIALOG
            if (isSneakPeekVisible && viewModel.selectedMarker != null) {
                SneakPeekMarkerCompose(
                    markerDto = viewModel.selectedMarker!!,
                    userPosition = viewModel.userPosition,
                    onExpandRequested = {
                        isSneakPeekVisible = false
                        isMarkerWindowVisable = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding() + 10.dp)
                )
            }

            // POPUPS & DIALOGE
            if (isMarkerWindowVisable) {
                MarkerWindow(
                    bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                    markerDto = viewModel.selectedMarker,
                    onDismiss = {
                        isMarkerWindowVisable = false
                        isSneakPeekVisible = false
                    },
                    onSave = { updatedDescription, updatedColor, newImageBytes ->
                        viewModel.selectedMarker?.let { currentMarker ->
                            viewModel.updateMarkerWithImage(
                                markerId = currentMarker.id ?: "",      // War vorher 'id'
                                description = updatedDescription,       // War vorher 'newDescription'
                                color = updatedColor,                   // War vorher 'newColor'
                                oldImageUrl = currentMarker.image_url,  // Kann direkt String? sein
                                newImageBytes = newImageBytes
                            )
                        }
                    },
                    onDelete = {
                        viewModel.selectedMarker?.let { currentMarker ->
                            viewModel.deleteMarker(currentMarker.id ?: "")
                        }
                    }
                )
            }

            if (isPoiWindowVisable) {
                POIWindow(
                    bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                    markerList = viewModel.markerList,
                    userPosition = viewModel.userPosition,
                    currentUserId = supabase.auth.currentUserOrNull()?.id,
                    onDismiss = { isPoiWindowVisable = false },
                    onPoiSelected = { selectedMarker ->
                        viewModel.selectMarker(selectedMarker)

                        val markerLat = selectedMarker.lat
                        val markerLon = selectedMarker.lon

                        coroutineScope.launch {
                            camera.animateTo(
                                CameraPosition(
                                    target = Position(latitude = markerLat, longitude = markerLon),
                                    zoom = 17.0
                                )
                            )
                        }
                    }
                )
            }

            if (isSettingWindowVisable) {
                SettingWindow(
                    bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                    onDismiss = { isSettingWindowVisable = false },
                    viewModel = viewModel
                )
            }
        }
    }
}