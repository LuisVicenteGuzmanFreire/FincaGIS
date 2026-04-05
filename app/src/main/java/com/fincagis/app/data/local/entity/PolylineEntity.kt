package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "polylines",
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
data class PolylineEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val name: String,
    val description: String? = null,
    val category: String = "General",
    val createdAt: Long
)
