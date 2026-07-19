package com.example.ap2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ap2.HomeScreenComposables.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ap2.MapScreenComposeables.MapMode
import com.example.ap2.MapScreenComposeables.MapScreen
import com.example.ap2.MapScreenComposeables.MapViewModel
import org.maplibre.spatialk.geojson.Position
import com.example.ap2.sensors.MotionRepository
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.auth

@Composable
fun HomeScreen(
    viewModel: MapViewModel = viewModel(),
    onNavigateToFriends: () -> Unit
) {
    var isSneakPeekVisible by remember { mutableStateOf(false) }

    val camera = org.maplibre.compose.camera.rememberCameraState(
        firstPosition = org.maplibre.compose.camera.CameraPosition(
            target = viewModel.userPosition,
            zoom = 16.5,
        )
    )
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current

    // 1. Initialisierung des Repositories
    val motionRepo = remember { MotionRepository(context) }

    // 2. Zustände für die Sensor-Werte
    var steps by remember { mutableStateOf(0f) }
    var compassDegree by remember { mutableStateOf(0f) }

    // 3. Sensoren starten und Daten sammeln (Flows abonnieren)
    LaunchedEffect(Unit) {
        // Schrittzähler sammeln
        launch {
            motionRepo.getStepCountUpdates().collect { newSteps ->
                steps = newSteps
            }
        }

        // Kompass (Azimuth) sammeln
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

    //Einteilung des HomeScreens in mehrere Sektionen (Hauptcontent, Bottom Navigation Bar)
    Scaffold(
        //Bottom Navigation Bar ohne buttons
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
            ){
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
                    onClick = { viewModel.startPlacingMode() } // Startet Platzierungsmodus
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
            // Leitet das geteilte ViewModel an den MapScreen weiter
            MapScreen(
                viewModel = viewModel,
                camera = camera,
                onMapClick = { pos ->
                    markerPosition = pos
                    // HIER die Änderungen, damit alles beim Klick verschwindet:
                    isSneakPeekVisible = false
                    isMarkerWindowVisable = false
                },
                onMarkerClick = {
                    // Beim Klick auf den Marker zeigen wir den SneakPeek
                    isSneakPeekVisible = true
                }
            )

            // 2. UI-Elemente
            if (viewModel.currentMode == MapMode.DEFAULT) {
                ProfileButton(
                    onClick = {},
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                )
            }

            //Kompass und Schritte und Handyausrichtung
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
                    // Schritte anzeigen
                    Text(
                        text = "Schritte: ${steps.toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Kompass-Icon
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_place_24),
                        contentDescription = "Kompass",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(40.dp)
                            // Die negative Drehung sorgt dafür, dass die Nadel immer nach Norden zeigt
                            .rotate(-compassDegree)
                    )

                    Text(
                        text = "${compassDegree.toInt()}°",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            if (isSneakPeekVisible && viewModel.selectedMarker != null) {
                SneakPeekMarkerCompose(
                    markerDto = viewModel.selectedMarker!!,
                    userPosition = viewModel.userPosition,
                    onExpandRequested = {
                        isSneakPeekVisible = false // Vorschau schließen
                        isMarkerWindowVisable = true // Volles Fenster öffnen
                    },
                    // Positionierung über der BottomBar
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding() + 10.dp)
                )
            }

            // 3. Popups/Fenster
            if (isMarkerWindowVisable) {
                MarkerWindow(
                    bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                    markerDto = viewModel.selectedMarker,
                    onDismiss = {
                        isMarkerWindowVisable = false
                        isSneakPeekVisible = false // Sicherstellen, dass auch die Vorschau geschlossen wird
                    },
                    onSave = { updatedDescription, updatedColor, newImageBytes ->
                        viewModel.selectedMarker?.let { currentMarker ->
                            viewModel.updateMarkerWithImage(
                                id = currentMarker.id ?: "",
                                newDescription = updatedDescription,
                                newColor = updatedColor,
                                oldImageUrl = currentMarker.image_url ?: "",
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

            // 3. Beim POIWindow-Klick die weiche Animation ausführen:
            if (isPoiWindowVisable) {
                POIWindow(
                    bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                    markerList = viewModel.markerList,
                    userPosition = viewModel.userPosition,
                    currentUserId = com.example.ap2.supabase.auth.currentUserOrNull()?.id?.toString(),
                    onDismiss = { isPoiWindowVisable = false },
                    onPoiSelected = { selectedMarker ->
                        viewModel.selectMarker(selectedMarker)

                        val markerLat = selectedMarker.lat
                        val markerLon = selectedMarker.lon

                        if (markerLat != null && markerLon != null) {
                            // EXAKTER AUFRUF: In der Coroutine die Kamera weich fliegen lassen
                            coroutineScope.launch {
                                camera.animateTo(
                                    org.maplibre.compose.camera.CameraPosition(
                                        target = Position(latitude = markerLat, longitude = markerLon),
                                        zoom = 17.0 // Wunsch-Zoom nach dem Flug
                                    )
                                )
                            }
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