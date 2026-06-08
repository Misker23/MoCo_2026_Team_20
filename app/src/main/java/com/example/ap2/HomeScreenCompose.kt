package com.example.ap2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
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
import com.example.ap2.ui.theme.MoCo_2026Theme

@Composable
fun HomeScreen() {
    //State Remember für MarkerScreen, ob dieser angezeigt wird oder nicht
    var isMarkerScreenVisible by remember { mutableStateOf(false) }
    //Einteilung des HomeScreens in mehrere Sektionen (Hauptcontent, Bottom Navigation Bar)
    Scaffold(
        //Bottom Navigation Bar ohne buttons
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.Gray))
        },
        //Add Marker button ohne Funktion
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_location_alt_24),
                    contentDescription = null
                )
            }
        }
        //Padding damit die Bottom Bar und Hauptcontent getrennt sind
    ) {contentPadding ->
        //um den Marker bisher in der Mitte anzuzeigen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //damit der initial Marker auf der "map" zu sehen ist und anklickbar ist
            SmallMarker(onExpandRequested = {isMarkerScreenVisible = true})
        }
        //hier damit der Screen vom Boden des Hauptcontents erscheint statt komplett unten oder vom Marker aus
        if (isMarkerScreenVisible) {
            OpenedMarker(
                //damit der Screen die Bottombar nicht überdeckt
                bottomPadding = contentPadding.calculateBottomPadding(),
                onDismiss = { isMarkerScreenVisible = false }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MoCo_2026Theme() {
        HomeScreen()
    }
}