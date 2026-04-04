package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polylines")
data class PolylineEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long
)