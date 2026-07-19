package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ap2.MarkerDto
import com.example.ap2.R // Stelle sicher, dass dies dein korrektes R-Package ist
import androidx.core.graphics.toColorInt

@Composable
fun SmallMarkerCompose(
    markerDto: MarkerDto,
    onExpandRequested: () -> Unit
) {
    val markerColor = remember(markerDto.color) {
        try {
            Color((markerDto.color ?: "#E91E63").toColorInt())
        } catch (e: Exception) {
            Color(0xFFE91E63)
        }
    }

    Image(
        painter = painterResource(id = R.drawable.baseline_place_24), // HIER Dateinamen prüfen!
        contentDescription = "Marker",
        colorFilter = ColorFilter.tint(markerColor), // Färbt das Icon dynamisch ein
        modifier = Modifier
            .size(32.dp) // Größe des Markers
            .clickable { onExpandRequested() }
    )
}