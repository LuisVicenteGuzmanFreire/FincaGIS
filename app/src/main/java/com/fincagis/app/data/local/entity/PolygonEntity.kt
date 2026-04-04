package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polygons")
data class PolygonEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val description: String?,
    val createdAt: Long
)