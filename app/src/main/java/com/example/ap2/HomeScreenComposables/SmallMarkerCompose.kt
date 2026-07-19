package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ap2.MarkerDto
import com.example.ap2.R
import androidx.core.graphics.toColorInt

@Composable
fun SmallMarker(markerDto: MarkerDto, onExpandRequested: () -> Unit) {
    var isSneakPeekVisible by remember { mutableStateOf(false) }

//Farbe für Marker bestimmen
    val markerColor = remember(markerDto.color) {
        try {
            val dbColor = markerDto.color ?: "#FF0000" //Standard Rot als Hex, falls leer
            //Prüfen, ob ein '#' fehlt (falls reiner Hex-Wert ohne Raute in der DB steht)
            val formattedColor = if (dbColor.startsWith("#") || dbColor.lowercase() in listOf("red", "blue", "green", "yellow", "purple", "black", "white")) {
                dbColor
            } else {
                "#$dbColor"
            }
            //Konvertiert den String sicher in eine Compose-Farbe
            Color(formattedColor.toColorInt())
        } catch (e: Exception) {
            Color.Red //Sicherer Fallback bei Tippfehlern in der DB
        }
    }

    Box {
        Icon(
            painter = painterResource(R.drawable.baseline_place_24),
            contentDescription = null,
            tint = markerColor, // JETZT DYNAMISCH
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isSneakPeekVisible = !isSneakPeekVisible },
        )

        if (isSneakPeekVisible) {
            Box(modifier = Modifier.offset(x = (-76).dp, y = (-128).dp)) {
                SneakPeekMarker(
                    markerDto = markerDto,
                    onDismiss = { isSneakPeekVisible = false },
                    onExpand = {
                        onExpandRequested()
                        isSneakPeekVisible = false
                    }
                )
            }
        }
    }
}