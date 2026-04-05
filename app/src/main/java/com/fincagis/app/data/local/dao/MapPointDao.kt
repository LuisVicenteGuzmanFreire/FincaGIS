package com.fincagis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fincagis.app.data.local.entity.MapPointEntity

@Dao
interface MapPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: MapPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<MapPointEntity>)

    @Query("SELECT * FROM map_points WHERE farmId = :farmId ORDER BY createdAt ASC")
    suspend fun getPointsByFarmId(farmId: String): List<MapPointEntity>

    @Query("""
        UPDATE map_points
        SET name = :name,
            description = :description,
            category = :category
        WHERE id = :pointId
    """)
    suspend fun updatePointAttributes(
        pointId: String,
        name: String,
        description: String?,
        category: String
    )

    @Query("""
        UPDATE map_points
        SET latitude = :latitude,
            longitude = :longitude
        WHERE id = :pointId
    """)
    suspend fun updatePointPosition(
        pointId: String,
        latitude: Double,
        longitude: Double
    )

    @Query("""
        UPDATE map_points
        SET photoPath = :photoPath,
            photoName = :photoName,
            photoCapturedAt = :photoCapturedAt,
            photoMimeType = :photoMimeType,
            photoSizeBytes = :photoSizeBytes
        WHERE id = :pointId
    """)
    suspend fun updatePointPhotoReference(
        pointId: String,
        photoPath: String?,
        photoName: String?,
        photoCapturedAt: Long?,
        photoMimeType: String?,
        photoSizeBytes: Long?
    )

    @Query("DELETE FROM map_points WHERE id = :pointId")
    suspend fun deleteById(pointId: String)

    @Query("DELETE FROM map_points WHERE farmId = :farmId")
    suspend fun deleteByFarmId(farmId: String)
}
