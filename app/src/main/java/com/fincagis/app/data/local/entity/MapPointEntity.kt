package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_points")
data class MapPointEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val description: String?,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long
)