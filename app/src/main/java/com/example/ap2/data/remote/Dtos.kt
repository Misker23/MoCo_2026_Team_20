package com.example.ap2.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkerDto(
    val id: String? = null, // Nullable, da neue Marker vor dem DB-Insert noch keine ID haben

    @SerialName("user_id")
    val user_id: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val description: String? = null,
    val color: String? = null,

    @SerialName("image_url")
    val image_url: String? = null,

    // Ignoriert/Fängt das automatische Supabase-Zeitstempel-Feld ab, falls vorhanden
    @SerialName("created_at")
    val createdAt: String? = null
)

// DTO für das Abfragen der Freundes-Farben
@Serializable
data class FriendColorDto(
    val friend_id: String,
    val color: String?
)

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class FriendDto(
    val user_id: String? = null,
    val friend_id: String,
    val color: String? = null,
    val status: String? = null,
    val profiles: ProfileDto? = null,
    val is_online: Boolean? = false
) {
    val displayName: String
        get() = profiles?.username ?: "Unbekannt"
}

@Serializable
data class SharedMarkerDto(
    @SerialName("marker_id")
    val markerId: String,

    @SerialName("friend_user_id")
    val friendUserId: String
)
