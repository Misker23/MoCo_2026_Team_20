package com.example.ap2

import kotlinx.serialization.Serializable

@Serializable
data class MarkerDto(
    val id: String? = null,          // uuid -> String
    val creator_id: String? = null,   // uuid -> String
    val color: String? = null,        // text
    val image_url: String? = null,    // text
    val description: String? = null,  // text
    val lat: Double,                 // float8 -> Double
    val lon: Double                  // float8 -> Double
)