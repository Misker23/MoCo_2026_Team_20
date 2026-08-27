package com.example.ap2.data_models

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

@Dao
interface MapDao {
    @Query("SELECT * FROM local_markers")
    fun getAllMarkersFlow(): Flow<List<LocalMarkerEntity>>

    @Query("SELECT * FROM local_markers WHERE isSynced = 0")
    suspend fun getUnsyncedMarkers(): List<LocalMarkerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMarker(marker: LocalMarkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkers(markers: List<LocalMarkerEntity>)

    @Insert
    suspend fun insertPendingFogPoint(point: PendingFogPointEntity)

    @Query("SELECT * FROM pending_fog_points ORDER BY timestamp ASC")
    suspend fun getPendingFogPoints(): List<PendingFogPointEntity>

    @Query("DELETE FROM pending_fog_points WHERE id IN (:ids)")
    suspend fun deletePendingFogPoints(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheFog(fogCache: FogCacheEntity)

    @Query("SELECT geoJson FROM fog_cache WHERE userId = :userId")
    fun getCachedFogFlow(userId: String): Flow<String?>

    @Query("DELETE FROM local_markers WHERE id = :id")
    suspend fun deleteMarkerById(id: String)

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
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "moco_map_db")
                    .fallbackToDestructiveMigration() // Verhindert Abstürze bei Datenbank-Schema-Änderungen
                    .build().also { INSTANCE = it }
            }
        }
    }
}