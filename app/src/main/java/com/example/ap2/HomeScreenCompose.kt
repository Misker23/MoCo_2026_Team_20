package com.example.ap2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ap2.HomeScreenComposables.*

@Composable
fun HomeScreen(
    onNavigateToFriends: () -> Unit
) {
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
                ProfileButton(onClick = {}, modifier = Modifier)
                POIButton(onClick = { isPoiWindowVisable = true }, modifier = Modifier)
                FriendsButton(onClick = onNavigateToFriends, modifier = Modifier)
                SettingButton(onClick = { isSettingWindowVisable = true }, modifier = Modifier)
            }
        }
    ) { contentPadding ->

        // Die Box sorgt dafür, dass die Karte im Hintergrund liegt
        // und die UI-Elemente darüber gestapelt werden.
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {

            // 1. Hintergrund: Die Karte
            MapScreen(
                onMapClick = { /* Hier könntest du auf Klicks reagieren */ }
            )

            // 2. UI-Elemente
            ProfileButton(
                onClick = {},
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )

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