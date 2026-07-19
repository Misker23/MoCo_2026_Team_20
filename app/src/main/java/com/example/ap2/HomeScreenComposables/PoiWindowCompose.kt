package com.example.ap2.HomeScreenComposables

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow // NEU: Für die drei Punkte (...) bei langem Text
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ap2.MarkerDto
import org.maplibre.spatialk.geojson.Position
import kotlin.math.*

@Composable
fun POIWindow(
    bottomPadding: Dp,
    markerList: List<MarkerDto>,
    userPosition: Position,
    currentUserId: String?,
    onDismiss: () -> Unit,
    onPoiSelected: (MarkerDto) -> Unit
) {
    val categories = listOf("Alle", "Meine Marker", "Von Freunden")
    var selectedCategory by remember { mutableStateOf("Alle") }

    val filteredMarkers = remember(selectedCategory, markerList, currentUserId) {
        when (selectedCategory) {
            "Meine Marker" -> markerList.filter { it.creator_id == currentUserId }
            "Von Freunden" -> markerList.filter { it.creator_id != currentUserId }
            else -> markerList
        }
    }

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .size(360.dp, 520.dp) // Leicht vergrößert für besseren Formfaktor
                .background(Color(0xFF1A1A1A).copy(alpha = 0.96f), RoundedCornerShape(24.dp)) // Modernere, rundere Ecken
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Oberer kleiner Indikator-Strich für edles Dialog-Design
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )

            Text(
                text = "Points of Interest",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Kategorien-Auswahl (Chips)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    SuggestionChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Medium) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) Color.White else Color(0xFF2D2D2D)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent
                        )
                    )
                }
            }

            // Scrollbare Liste
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 10.dp), // Verhindert Abschneiden des letzten Elements
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredMarkers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Keine Einträge in dieser Kategorie.", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredMarkers) { marker ->
                        PoiCard(
                            marker = marker,
                            userPosition = userPosition,
                            isOwnMarker = marker.creator_id == currentUserId,
                            onClick = {
                                onPoiSelected(marker)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PoiCard(marker: MarkerDto, userPosition: Position, isOwnMarker: Boolean, onClick: () -> Unit) {
    val distanceText = remember(marker.lat, marker.lon, userPosition) {
        if (marker.lat != null && marker.lon != null) {
            calculateDistance(userPosition.latitude, userPosition.longitude, marker.lat, marker.lon)
        } else {
            "-- km"
        }
    }

    val markerColor = remember(marker.color) {
        try {
            Color(android.graphics.Color.parseColor(marker.color ?: "#FF0000"))
        } catch (e: Exception) {
            Color.Red
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)), // Dezenterer Kontrast
        shape = RoundedCornerShape(14.dp) // Weichere Kanten für die Karten
    ) {
        Row(
            modifier = Modifier.padding(16.dp), // Mehr Innenabstand für edlere Optik
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Farbiger Punkt
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(markerColor, CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                // KORRIGIERTE REIHE: Verhindert das Quetschen der Distanz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = marker.description?.takeIf { it.isNotBlank() } ?: "Marker ohne Namen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis, // Fügt "..." hinzu, falls der Text zu lang ist
                        modifier = Modifier.weight(1f) // Nimmt sich nur den verfügbaren Platz links
                    )

                    Spacer(modifier = Modifier.width(12.dp)) // Erzwingt Mindestabstand

                    Text(
                        text = distanceText,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1 // Verhindert vertikales Umbrechen komplett
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isOwnMarker) "Eigener Marker" else "Von einem Freund",
                    color = if (isOwnMarker) Color(0xFF81C784) else Color(0xFF64B5F6), // Angenehmere, soft-matte Farben
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Haversine-Formel zur Distanzberechnung zwischen zwei GPS-Koordinaten
@SuppressLint("DefaultLocale")
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
    val earthRadius = 6371.0 // km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distanceKm = earthRadius * c

    return if (distanceKm < 1.0) {
        "${(distanceKm * 1000).toInt()} m"
    } else {
        String.format("%.1f km", distanceKm)
    }
}