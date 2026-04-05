package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "map_points",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]
)
data class MapPointEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val description: String?,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    // Absolute local path (private app storage). Not intended for export as-is.
    val photoPath: String? = null,
    // Stable exportable file name (used later by KMZ exporter when packaging media).
    val photoName: String? = null,
    val photoCapturedAt: Long? = null,
    val photoMimeType: String? = null,
    val photoSizeBytes: Long? = null
)
