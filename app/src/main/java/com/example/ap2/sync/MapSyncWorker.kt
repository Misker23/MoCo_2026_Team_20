package com.example.ap2.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ap2.data_models.AppDatabase
import com.example.ap2.data_models.FogCacheEntity
import com.example.ap2.data_models.LocalMarkerEntity
import com.example.ap2.data_models.MarkerDto
import com.example.ap2.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID


class MapSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val user = supabase.auth.currentUserOrNull() ?: return@withContext Result.success()
        val dao = AppDatabase.getDatabase(applicationContext).mapDao()

        try {
            // --- A. Ausstehende Löschungen auf Supabase spiegeln ---
            val pendingDeletions = dao.getPendingDeletedMarkers()
            if (pendingDeletions.isNotEmpty()) {
                val deletedIds = pendingDeletions.map { it.id }
                for (id in deletedIds) {
                    supabase.postgrest.from("markers").delete {
                        filter { eq("id", id) }
                    }
                }
                // Nach erfolgreichem Server-Delete aus der Warteschlange entfernen
                dao.deletePendingDeletedMarkers(deletedIds)
            }

            // 2. Offline Fog-Punkte per RPC übertragen[cite: 4]
            val pendingFog = dao.getPendingFogPoints()
            if (pendingFog.isNotEmpty()) {
                val ids = pendingFog.map { point ->
                    supabase.postgrest.rpc("add_fog_point", buildJsonObject {
                        put("new_lat", point.lat)
                        put("new_lon", point.lon)
                    })
                    point.id
                }
                dao.deletePendingFogPoints(ids)
            }

            // 3. Neueste Server-Daten synchronisieren[cite: 3, 4, 8]
            val remoteMarkers = supabase.postgrest.from("markers").select().decodeList<MarkerDto>()
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

            // 4. Fog-Stand abrufen & cachen[cite: 3, 4]
            supabase.postgrest.rpc("ensure_user_fog")
            val fogResult = supabase.postgrest.rpc("get_user_fog")
            dao.cacheFog(FogCacheEntity(userId = user.id, geoJson = fogResult.data))

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}