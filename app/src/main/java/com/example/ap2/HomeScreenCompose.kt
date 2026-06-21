package com.example.ap2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.HomeScreenComposables.FriendsButton
import com.example.ap2.HomeScreenComposables.MarkerWindow
import com.example.ap2.HomeScreenComposables.POIButton
import com.example.ap2.HomeScreenComposables.POIWindow
import com.example.ap2.HomeScreenComposables.ProfileButton
import com.example.ap2.HomeScreenComposables.SettingButton
import com.example.ap2.HomeScreenComposables.SettingWindow
import com.example.ap2.HomeScreenComposables.SmallMarker
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun HomeScreen(
    onNavigateToFriends: () -> Unit // Parameter für Navigation hinzugefügt
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

                // Hier das Lambda übergeben
                FriendsButton(onClick = onNavigateToFriends, modifier = Modifier)

                SettingButton(onClick = { isSettingWindowVisable = true }, modifier = Modifier)
            }
        }
    ) { contentPadding ->

        ProfileButton(
            onClick = {},
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp)
        )
        //um den Marker bisher in der Mitte anzuzeigen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //damit der initiale Marker auf der "map" zu sehen ist und anklickbar ist
            SmallMarker(onExpandRequested = { isMarkerWindowVisable = true })
        }
        //hier damit der Screen vom Boden des Hauptcontents erscheint statt komplett unten oder vom Marker aus
        if (isMarkerWindowVisable) {
            MarkerWindow(
                //damit der Screen die Bottombar nicht überdeckt
                bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                onDismiss = { isMarkerWindowVisable = false }
            )
        }

        if (isPoiWindowVisable) {
            POIWindow(
                //damit der Screen die Bottombar nicht überdeckt
                bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                onDismiss = { isPoiWindowVisable = false }
            )
        }

        if (isSettingWindowVisable) {
            SettingWindow(
                //damit der Screen die Bottombar nicht überdeckt
                bottomPadding = contentPadding.calculateBottomPadding() + 10.dp,
                onDismiss = { isSettingWindowVisable = false }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MoCo_2026Theme() {
        HomeScreen(onNavigateToFriends = {})
    }
}