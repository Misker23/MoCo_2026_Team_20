package com.example.ap2.repositories

import android.content.Context
import androidx.work.*
import com.example.ap2.data_models.*
import com.example.ap2.sync.MapSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MapRepository(private val context: Context) {
    private val dao = AppDatabase.getDatabase(context).mapDao()

    // Nur EINE Instanz von getMarkersFlow()
    fun getMarkersFlow(): Flow<List<MarkerDto>> = dao.getAllMarkersFlow().map { list ->
        list.map { MarkerDto(it.id, it.userId, it.lat, it.lon, it.description, it.color, it.imageUrl) }
    }

    fun getFogFlow(userId: String): Flow<String?> = dao.getCachedFogFlow(userId)

    suspend fun saveMarkerLocally(
        userId: String,
        lat: Double,
        lon: Double,
        desc: String,
        color: String,
        imageBytes: ByteArray? = null,
        existingImageUrl: String? = null,
        markerId: String = UUID.randomUUID().toString()
    ) = withContext(Dispatchers.IO) {
        var localImagePath: String? = existingImageUrl

        // 1. Neues Bild offline im internen App-Speicher sichern
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val imageDir = File(context.filesDir, "marker_images").apply { mkdirs() }
            val imageFile = File(imageDir, "marker_${UUID.randomUUID()}.jpg")
            imageFile.writeBytes(imageBytes)
            localImagePath = imageFile.absolutePath
        }

        // 2. Lokalen Pfad/URL in Room hinterlegen und Sync anstoßen
        val entity = LocalMarkerEntity(
            id = markerId,
            userId = userId,
            lat = lat,
            lon = lon,
            description = desc,
            color = color,
            imageUrl = localImagePath,
            isSynced = false
        )
        dao.insertOrUpdateMarker(entity)
        triggerBackgroundSync()
    }

    suspend fun deleteMarker(id: String) = withContext(Dispatchers.IO) {
        // 1. Aus der lokalen Room-Datenbank löschen
        dao.deleteMarkerById(id)

        // 2. ID in die Warteschlange für den Server-Sync eintragen
        dao.insertPendingDeletedMarker(PendingDeletedMarkerEntity(id))

        // 3. Background Sync ausführen
        triggerBackgroundSync()
    }

    suspend fun recordFogPointLocally(lat: Double, lon: Double) = withContext(Dispatchers.IO) {
        dao.insertPendingFogPoint(PendingFogPointEntity(lat = lat, lon = lon))
        triggerBackgroundSync()
    }

    fun triggerBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<MapSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "MapSyncWork",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}