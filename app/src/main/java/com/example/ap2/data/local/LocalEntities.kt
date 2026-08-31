package com.example.ap2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_markers")
data class LocalMarkerEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val lat: Double,
    val lon: Double,
    val description: String?,
    val color: String?,
    val imageUrl: String?,
    val isSynced: Boolean = false
)

@Entity(tableName = "pending_fog_points")
data class PendingFogPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lat: Double,
    val lon: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fog_cache")
data class FogCacheEntity(
    @PrimaryKey val userId: String,
    val geoJson: String
)

@Entity(tableName = "pending_deleted_markers")
data class PendingDeletedMarkerEntity(
    @PrimaryKey val id: String
)