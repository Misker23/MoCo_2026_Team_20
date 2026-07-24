package com.example.ap2.homeScreenComposables

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    viewModel: MapViewModel = viewModel(),
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

    // 1. Initialisierung Sensoren & Daten-Upload
    val motionRepo = remember { MotionRepository(context) }
    var steps by remember { mutableFloatStateOf(0f) }
    var compassDegree by remember { mutableFloatStateOf(0f) }

    // 2. Initialer Start: Marker laden & Sensoren abonnieren
    LaunchedEffect(Unit) {
        // Marker aus Supabase laden
        viewModel.loadMarkersForMap()

        // Schritte sammeln
        launch {
            motionRepo.getStepCountUpdates().collect { newSteps ->
                steps = newSteps
            }
        }

        // Kompass sammeln
        launch {
            motionRepo.getCompassUpdates().collect { azimuth ->
                compassDegree = azimuth
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
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                POIButton(
                    onClick = { isPoiWindowVisable = true },
                    modifier = Modifier.weight(1f)
                )
                FriendsButton(
                    onClick = { onNavigateToFriends() },
                    modifier = Modifier.weight(1f)
                )
                SettingButton(
                    onClick = { isSettingWindowVisable = true },
                    modifier = Modifier.weight(1f)
                )
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
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                )
            }

            // KOMPASS & SCHRITTE
            if (viewModel.currentMode == MapMode.DEFAULT) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Schritte: ${steps.toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.baseline_place_24),
                        contentDescription = "Kompass",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(-compassDegree)
                    )

                    Text(
                        text = "${compassDegree.toInt()}°",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // PLATZIERUNGS-MODUS OVERLAY (Wenn neuer Marker gesetzt werden soll)
            if (viewModel.currentMode == MapMode.PLACING_MARKER) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.resetMode() }) {
                        Icon(Icons.Default.Close, contentDescription = "Abbrechen", tint = Color.Red)
                    }

                    Text("Marker-Position wählen", color = Color.White, fontSize = 14.sp)

                    IconButton(
                        onClick = {
                            // Erstellt einen Marker an der aktuellen Kamera/Klick-Position
                            markerPosition?.let { pos ->
                                // hier kann deine Methode zum Speichern aufgerufen werden
                            }
                            viewModel.resetMode()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Bestätigen", tint = Color.Green)
                    }
                }
            }

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
                    onDismiss = { isSettingWindowVisable = false }
                )
            }
        }
    }
}