package com.example.ap2.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ap2.data.local.AppDatabase
import com.example.ap2.data.local.FogCacheEntity
import com.example.ap2.data.local.LocalMarkerEntity
import com.example.ap2.data.remote.MarkerDto
import com.example.ap2.data.remote.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID

class MapSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val user = supabase.auth.currentUserOrNull() ?: return@withContext Result.success()
        val dao = AppDatabase.getDatabase(applicationContext).mapDao()

        try {
            // 1. Ausstehende Löschungen auf Supabase spiegeln
            val pendingDeletions = dao.getPendingDeletedMarkers()
            if (pendingDeletions.isNotEmpty()) {
                val deletedIds = pendingDeletions.map { it.id }
                for (id in deletedIds) {
                    supabase.postgrest.from("markers").delete {
                        filter { eq("id", id) }
                    }
                }
                dao.deletePendingDeletedMarkers(deletedIds)
            }

            // 2. Unsynced Marker hochladen
            val unsyncedMarkers = dao.getUnsyncedMarkers()
            for (marker in unsyncedMarkers) {
                var remoteImageUrl = marker.imageUrl

                // Lokales Bild in Supabase Storage hochladen
                if (!marker.imageUrl.isNullOrEmpty() && !marker.imageUrl.startsWith("http")) {
                    val localFile = File(marker.imageUrl)
                    if (localFile.exists()) {
                        try {
                            val fileName = "marker_${marker.id}.jpg"
                            val bucket = supabase.storage.from("marker-images")
                            bucket.upload(fileName, localFile.readBytes(), upsert = true)
                            remoteImageUrl = bucket.publicUrl(fileName)
                        } catch (e: Exception) {
                            Log.e("MapSyncWorker", "Fehler beim Bild-Upload: ${e.message}")
                        }
                    }
                }

                // Payload für PostGIS aufbereiten (OHNE 'position', übernimmt der DB-Trigger!)
                val payload = buildJsonObject {
                    put("id", marker.id)
                    put("user_id", marker.userId)
                    put("lat", marker.lat)
                    put("lon", marker.lon)
                    marker.description?.let { put("description", it) }
                    marker.color?.let { put("color", it) }
                    remoteImageUrl?.let { put("image_url", it) }
                }

                supabase.postgrest.from("markers").upsert(payload)

                // Lokal als synchronisiert markieren
                dao.insertOrUpdateMarker(
                    marker.copy(
                        imageUrl = remoteImageUrl,
                        isSynced = true
                    )
                )
            }

            // 3. Offline Fog-Punkte per RPC übertragen
            val pendingFog = dao.getPendingFogPoints()
            if (pendingFog.isNotEmpty()) {
                val ids = mutableListOf<Long>()
                for (point in pendingFog) {
                    try {
                        supabase.postgrest.rpc("add_fog_point", buildJsonObject {
                            put("new_lat", point.lat)
                            put("new_lon", point.lon)
                        })
                        ids.add(point.id)
                    } catch (e: Exception) {
                        Log.e("MapSyncWorker", "Fog-RPC Fehler: ${e.message}")
                    }
                }
                if (ids.isNotEmpty()) {
                    dao.deletePendingFogPoints(ids)
                }
            }

            // 4. Neueste Server-Daten synchronisieren (Eigene + geteilte Marker)
            val remoteMarkers = supabase.postgrest.from("markers").select().decodeList<MarkerDto>()
            val remoteIds = remoteMarkers.mapNotNull { it.id }.toSet()

            // Marker entfernen, deren Freigabe entzogen oder die auf dem Server gelöscht wurden
            val localSyncedMarkers = dao.getSyncedMarkers()
            val markersToRemove = localSyncedMarkers.filter { it.id !in remoteIds }
            for (staleMarker in markersToRemove) {
                dao.deleteMarkerById(staleMarker.id)
            }

            // Aktuelle Server-Marker lokal in Room aktualisieren
            val entities = withContext(Dispatchers.Default) {
                remoteMarkers.map { dto ->
                    LocalMarkerEntity(
                        id = dto.id ?: UUID.randomUUID().toString(),
                        userId = dto.user_id,
                        lat = dto.lat,
                        lon = dto.lon,
                        description = dto.description,
                        color = dto.color,
                        imageUrl = dto.image_url,
                        isSynced = true
                    )
                }
            }
            dao.insertMarkers(entities)

            // 5. Fog-Stand abrufen & cachen
            supabase.postgrest.rpc("ensure_user_fog")
            val fogResult = supabase.postgrest.rpc("get_user_fog")
            dao.cacheFog(FogCacheEntity(userId = user.id, geoJson = fogResult.data))

            Result.success()
        } catch (e: Exception) {
            Log.e("MapSyncWorker", "Sync fehlgeschlagen: ${e.message}", e)
            Result.retry()
        }
    }
}