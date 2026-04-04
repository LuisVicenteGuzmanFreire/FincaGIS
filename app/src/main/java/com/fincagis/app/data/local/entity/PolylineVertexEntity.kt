package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polyline_vertices")
data class PolylineVertexEntity(
    @PrimaryKey
    val id: String,
    val polylineId: String,
    val vertexOrder: Int,
    val latitude: Double,
    val longitude: Double
)