package com.fincagis.app.presentation.main.map

import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadCompleteMapData(
    db: AppDatabase,
    farmId: String
): MapScreenData {
    return withContext(Dispatchers.IO) {
        val points = db.mapPointDao().getPointsByFarmId(farmId)

        val polygons = db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val vertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to vertices
        }

        val polylines = db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }

        MapScreenData(
            points = points,
            polygons = polygons,
            polylines = polylines
        )
    }
}

suspend fun loadMapData(
    db: AppDatabase,
    farmId: String
): Pair<List<MapPointEntity>, List<Pair<PolygonEntity, List<PolygonVertexEntity>>>> {
    val data = loadCompleteMapData(db = db, farmId = farmId)
    return data.points to data.polygons
}

suspend fun loadPolylineData(
    db: AppDatabase,
    farmId: String
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }
    }
}

