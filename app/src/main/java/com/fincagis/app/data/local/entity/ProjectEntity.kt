package com.fincagis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val description: String?,
    val createdAt: Long
)