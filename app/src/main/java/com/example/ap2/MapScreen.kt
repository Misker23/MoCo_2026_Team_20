package com.example.ap2

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapScreen(
    onMapClick: (Position) -> Unit,
) {
    // erzeugt Kamera und gibt ihr eine Startposition
    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(
                latitude = 51.023215,
                longitude = 7.56198,
            ),
            zoom = 16.5,
        )
    )


    // speichert Daten eines Klicks
    /*var clickedPosition by remember { mutableStateOf<Position?>(null) }

    // erzeugt eine sanfte Animation zur neuen Position
    LaunchedEffect(clickedPosition) {
        clickedPosition?.let { pos ->
            camera.animateTo(
                finalPosition = camera.position.copy(
                    target = pos,
                    zoom = 14.0
                ),
                duration = 1.5.seconds
            )
        }
    }*/

    //UI Anpassung
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ){
        //bindet MapLibre ein
        MaplibreMap(
            //baseStyle = BaseStyle.Uri(
            //    "https://api.protomaps.com/styles/v4/$variant/en.json?key=MY_KEY"),

            //definiert den visuellen Stil der Map
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),

            cameraState = camera,

            //Speichert aktuelle Position
            onMapClick = { pos, offset ->

                //clickedPosition = pos
                onMapClick(pos)
                Log.d("MapScreen", "Position: $pos")
                ClickResult.Consume
            },

            options =
                MapOptions(
                    //definiert erlaubte Gesten der Karte
                    gestureOptions =
                        GestureOptions(
                            isTiltEnabled = false,
                            isZoomEnabled = true,
                            isRotateEnabled = true,
                            isScrollEnabled = true,
                        ),
                    //definiert angezeigte Overlays
                    ornamentOptions =
                        OrnamentOptions(
                            isCompassEnabled = true,
                            compassAlignment = Alignment.TopEnd,
                            isScaleBarEnabled = true,
                            scaleBarAlignment = Alignment.TopStart,

                            ),

                    )
        )

    }
    //val variant = if (isSystemInDarkTheme()) "dark" else "light"

}