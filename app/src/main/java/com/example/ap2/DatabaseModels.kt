package com.example.ap2

import kotlinx.serialization.Serializable

@Serializable
data class MarkerDto(
    val id: String? = null,
    val creator_id: String? = null,
    val color: String? = "red",
    val description: String? = null,
    val image_url: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)