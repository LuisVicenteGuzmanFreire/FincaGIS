package com.fincagis.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fincagis.app.data.local.dao.FarmDao
import com.fincagis.app.data.local.dao.MapPointDao
import com.fincagis.app.data.local.dao.PolygonDao
import com.fincagis.app.data.local.dao.ProjectDao
import com.fincagis.app.data.local.dao.UserDao
import com.fincagis.app.data.local.entity.FarmEntity
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.ProjectEntity
import com.fincagis.app.data.local.entity.UserEntity
import com.fincagis.app.data.local.dao.PolylineDao
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

@Database(
    entities = [
        UserEntity::class,
        FarmEntity::class,
        ProjectEntity::class,
        MapPointEntity::class,
        PolygonEntity::class,
        PolygonVertexEntity::class,
        PolylineEntity::class,
        PolylineVertexEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun farmDao(): FarmDao
    abstract fun projectDao(): ProjectDao
    abstract fun mapPointDao(): MapPointDao
    abstract fun polygonDao(): PolygonDao
    abstract fun polylineDao(): PolylineDao
}
