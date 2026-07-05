package com.example.ap2

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ap2.HomeScreenComposables.*
import com.example.ap2.sensors.MotionRepository
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToFriends: () -> Unit
) {
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


    var isMarkerWindowVisable by remember { mutableStateOf(false) }
    var isPoiWindowVisable by remember { mutableStateOf(false) }
    var isSettingWindowVisable by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                POIButton(onClick = { isPoiWindowVisable = true }, modifier = Modifier)
                FriendsButton(onClick = onNavigateToFriends, modifier = Modifier)
                SettingButton(onClick = { isSettingWindowVisable = true }, modifier = Modifier)
            }
        }
    ) { contentPadding ->

        // Die Box sorgt dafür, dass die Karte im Hintergrund liegt
        // und die UI-Elemente darüber gestapelt werden.
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)) {

            // 1. Hintergrund: Die Karte
            MapScreen(
                onMapClick = { /* Hier könntest du auf Klicks reagieren */ }
            )

            // 2. UI-Elemente
            ProfileButton(
                onClick = {},
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
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
                    painter = painterResource(id = R.drawable.baseline_place_24), // Beispiel-Icon
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
            // HIER WIEDER HINZUFÜGEN:
            // Der Marker wird nun mittig über der Karte platziert
            Box(
                modifier = Modifier
                    .fillMaxSize(), // Füllt den Bereich der Box aus
                contentAlignment = Alignment.Center // Zentriert den Inhalt
            ) {
                SmallMarker(
                    onExpandRequested = { isMarkerWindowVisable = true }
                )
            }
        }

        // 3. Popups/Fenster (erscheinen über allem)
        if (isMarkerWindowVisable) {
            MarkerWindow(
                bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                onDismiss = { isMarkerWindowVisable = false }
            )
        }

        if (isPoiWindowVisable) {
            POIWindow(
                bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                onDismiss = { isPoiWindowVisable = false }
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

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(onNavigateToFriends = {})
}