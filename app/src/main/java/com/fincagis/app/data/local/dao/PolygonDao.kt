package com.fincagis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity

@Dao
interface PolygonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolygon(polygon: PolygonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVertices(vertices: List<PolygonVertexEntity>)

    @Query("SELECT * FROM polygons WHERE farmId = :farmId ORDER BY createdAt ASC")
    suspend fun getPolygonsByFarmId(farmId: String): List<PolygonEntity>

    @Query("SELECT * FROM polygon_vertices WHERE polygonId = :polygonId ORDER BY vertexOrder ASC")
    suspend fun getVerticesByPolygonId(polygonId: String): List<PolygonVertexEntity>

    @Query("DELETE FROM polygon_vertices WHERE polygonId = :polygonId")
    suspend fun deleteVerticesByPolygonId(polygonId: String)

    @Query("""
        UPDATE polygons
        SET name = :name,
            description = :description,
            category = :category
        WHERE id = :polygonId
    """)
    suspend fun updatePolygonAttributes(
        polygonId: String,
        name: String,
        description: String?,
        category: String
    )

    @Query("DELETE FROM polygons WHERE id = :polygonId")
    suspend fun deletePolygonById(polygonId: String)

    @Query("DELETE FROM polygons WHERE farmId = :farmId")
    suspend fun deletePolygonsByFarmId(farmId: String)
}
