package com.example.ap2.homeScreenComposables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Wichtig für Bilder[cite: 1]
import com.example.ap2.data_models.MarkerDto

@Composable
fun SneakPeekMarkerCompose(
    markerDto: MarkerDto,
    userPosition: org.maplibre.spatialk.geojson.Position,
    onExpandRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val markerColor = remember(markerDto.color) {
        try {
            Color(android.graphics.Color.parseColor(markerDto.color ?: "#E91E63"))
        } catch (e: Exception) {
            Color.Red
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onExpandRequested() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.96f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.height(100.dp), // Feste Höhe für die Vorschau
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bild-Vorschau
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
            ) {
                if (!markerDto.image_url.isNullOrEmpty()) {
                    AsyncImage(
                        model = markerDto.image_url,
                        contentDescription = "Marker Bild",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback, falls kein Bild da ist
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Kein Bild", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            // Text-Informationen
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = markerDto.description?.takeIf { it.isNotBlank() } ?: "Marker ohne Titel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tippen für Details...",
                    color = markerColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}