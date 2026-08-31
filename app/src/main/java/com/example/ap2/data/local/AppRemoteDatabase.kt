package com.example.ap2.data.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Fertiges UI-Modell für eine Map/Compose
data class MapMarkerUiState(
    val id: String,
    val creatorId: String,
    val lat: Double,
    val lon: Double,
    val description: String?,
    val imageUrl: String? = null, // Ergänzt, damit Bilder auf der Karte verfügbar bleiben
    val displayColor: String,     // Die finale Farbe (eigene Marker-Farbe ODER Freundes-Farbe)
    val isOwnMarker: Boolean
)

@Serializable
data class SharedMarkerDto(
    @SerialName("marker_id")
    val markerId: String,

    @SerialName("friend_user_id")
    val friendUserId: String
)