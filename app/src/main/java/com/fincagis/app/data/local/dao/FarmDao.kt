package com.fincagis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fincagis.app.data.local.entity.FarmEntity

@Dao
interface FarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(farm: FarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(farms: List<FarmEntity>)

    @Query("SELECT * FROM farms ORDER BY createdAt DESC")
    suspend fun getAllFarms(): List<FarmEntity>

    @Query("SELECT * FROM farms WHERE id = :farmId LIMIT 1")
    suspend fun getFarmById(farmId: String): FarmEntity?

    @Query("DELETE FROM farms")
    suspend fun deleteAll()
}