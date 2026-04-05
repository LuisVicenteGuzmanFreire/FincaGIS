package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "polygon_vertices",
    foreignKeys = [
        ForeignKey(
            entity = PolygonEntity::class,
            parentColumns = ["id"],
            childColumns = ["polygonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["polygonId"])]
)
data class PolygonVertexEntity(
    @PrimaryKey val id: String,
    val polygonId: String,
    val vertexOrder: Int,
    val latitude: Double,
    val longitude: Double
)
