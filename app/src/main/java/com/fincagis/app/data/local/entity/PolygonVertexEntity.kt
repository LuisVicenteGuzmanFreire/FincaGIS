package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polygon_vertices")
data class PolygonVertexEntity(
    @PrimaryKey val id: String,
    val polygonId: String,
    val vertexOrder: Int,
    val latitude: Double,
    val longitude: Double
)