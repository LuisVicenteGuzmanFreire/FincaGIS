package com.fincagis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fincagis.app.data.local.entity.ProjectEntity

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)

    @Query("SELECT * FROM projects WHERE farmId = :farmId")
    suspend fun getProjectsByFarmId(farmId: String): List<ProjectEntity>

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}