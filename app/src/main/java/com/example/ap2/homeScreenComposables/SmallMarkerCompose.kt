package com.example.ap2.homeScreenComposables

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
import androidx.core.graphics.toColorInt
import com.example.ap2.R
import com.example.ap2.data.remote.MarkerDto

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
        painter = painterResource(id = R.drawable.baseline_place_24),
        contentDescription = "Marker",
        colorFilter = ColorFilter.tint(markerColor),
        modifier = Modifier
            .size(32.dp)
            .clickable { onExpandRequested() }
    )
}