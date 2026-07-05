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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // WICHTIGER IMPORT!
import com.example.ap2.HomeScreenComposables.FriendsButton
import com.example.ap2.HomeScreenComposables.MarkerWindow
import com.example.ap2.HomeScreenComposables.POIButton
import com.example.ap2.HomeScreenComposables.POIWindow
import com.example.ap2.HomeScreenComposables.ProfileButton
import com.example.ap2.HomeScreenComposables.SettingButton
import com.example.ap2.HomeScreenComposables.SettingWindow
import com.example.ap2.MapScreenComposeables.MapMode
import com.example.ap2.MapScreenComposeables.MapScreen
import com.example.ap2.MapScreenComposeables.MapViewModel
import org.maplibre.spatialk.geojson.Position

@Composable
fun HomeScreen(
    viewModel: MapViewModel = viewModel()
) {
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
            ){
                POIButton(
                    onClick = { isPoiWindowVisable = true },
                    modifier = Modifier.weight(1f)
                )
                FriendsButton(
                    onClick = {},
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
                onMapClick = { pos ->
                    markerPosition = pos
                },
                onMarkerClick = {
                    isMarkerWindowVisable = true
                }
            )

            ProfileButton(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )

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
}