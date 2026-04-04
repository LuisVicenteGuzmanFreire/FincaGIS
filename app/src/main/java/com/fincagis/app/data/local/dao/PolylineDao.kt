package com.fincagis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

@Dao
interface PolylineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolyline(polyline: PolylineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVertices(vertices: List<PolylineVertexEntity>)

    @Query("SELECT * FROM polylines WHERE farmId = :farmId ORDER BY createdAt DESC")
    suspend fun getPolylinesByFarmId(farmId: String): List<PolylineEntity>

    @Query("SELECT * FROM polyline_vertices WHERE polylineId = :polylineId ORDER BY vertexOrder ASC")
    suspend fun getVerticesByPolylineId(polylineId: String): List<PolylineVertexEntity>

    @Query("DELETE FROM polyline_vertices WHERE polylineId = :polylineId")
    suspend fun deleteVerticesByPolylineId(polylineId: String)

    @Query("DELETE FROM polylines WHERE id = :polylineId")
    suspend fun deletePolylineById(polylineId: String)
}