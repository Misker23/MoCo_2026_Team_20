package com.example.ap2.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MapDao {

    // Nur Marker des aktuellen Nutzers abfragen
    @Query("SELECT * FROM local_markers")
    fun getAllMarkersFlow(): Flow<List<LocalMarkerEntity>>

    @Query("SELECT * FROM local_markers WHERE isSynced = 0")
    suspend fun getUnsyncedMarkers(): List<LocalMarkerEntity>

    //Holt alle bisher vom Server synchronisierten Marker zum Abgleich
    @Query("SELECT * FROM local_markers WHERE isSynced = 1")
    suspend fun getSyncedMarkers(): List<LocalMarkerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMarker(marker: LocalMarkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkers(markers: List<LocalMarkerEntity>)

    @Query("DELETE FROM local_markers WHERE id = :id")
    suspend fun deleteMarkerById(id: String)

    @Query("DELETE FROM local_markers")
    suspend fun clearAllMarkers()

    @Query("DELETE FROM fog_cache")
    suspend fun clearFogCache()

    @Insert
    suspend fun insertPendingFogPoint(point: PendingFogPointEntity)

    @Query("SELECT * FROM pending_fog_points ORDER BY timestamp ASC")
    suspend fun getPendingFogPoints(): List<PendingFogPointEntity>

    @Query("DELETE FROM pending_fog_points WHERE id IN (:ids)")
    suspend fun deletePendingFogPoints(ids: List<Long>)

    @Query("DELETE FROM pending_fog_points")
    suspend fun clearPendingFogPoints()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheFog(fogCache: FogCacheEntity)

    @Query("SELECT geoJson FROM fog_cache WHERE userId = :userId")
    fun getCachedFogFlow(userId: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertPendingDeletedMarker(entity: PendingDeletedMarkerEntity)

@Query("SELECT * FROM pending_deleted_markers")
suspend fun getPendingDeletedMarkers(): List<PendingDeletedMarkerEntity>

@Query("DELETE FROM pending_deleted_markers WHERE id IN (:ids)")
suspend fun deletePendingDeletedMarkers(ids: List<String>)
}

@Database(
    entities = [
        LocalMarkerEntity::class,
        PendingFogPointEntity::class,
        FogCacheEntity::class,
        PendingDeletedMarkerEntity::class // <-- Hinzugefügt
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moco_map_db"
                )
                    .fallbackToDestructiveMigration(false) // Verhindert Abstürze bei Datenbank-Schema-Änderungen
                    .build().also { INSTANCE = it }
            }
        }
    }
}