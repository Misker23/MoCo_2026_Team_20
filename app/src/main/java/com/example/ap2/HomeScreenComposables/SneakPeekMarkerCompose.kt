package com.example.ap2.HomeScreenComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ap2.MarkerDto

@Composable
fun SneakPeekMarker(markerDto: MarkerDto, onDismiss: () -> Unit, onExpand: () -> Unit) {
    // KEIN Popup mehr! Die Box ist jetzt das direkte UI-Element.
    Box(
        modifier = Modifier
            .size(200.dp, 120.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onExpand() } // Klick leitet sauber weiter!
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp, 60.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (!markerDto.image_url.isNullOrEmpty()) {
                    AsyncImage(
                        model = markerDto.image_url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                }
            }

            Text(
                text = markerDto.description ?: "Keine Beschreibung",
                modifier = Modifier.padding(vertical = 4.dp),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}