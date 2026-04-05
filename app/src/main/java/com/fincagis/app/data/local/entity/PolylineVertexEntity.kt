package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "polyline_vertices",
    foreignKeys = [
        ForeignKey(
            entity = PolylineEntity::class,
            parentColumns = ["id"],
            childColumns = ["polylineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["polylineId"])]
)
data class PolylineVertexEntity(
    @PrimaryKey
    val id: String,
    val polylineId: String,
    val vertexOrder: Int,
    val latitude: Double,
    val longitude: Double
)
