package com.fincagis.app.presentation.main.map

import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

fun getVerticesOfSelectedPolygon(
    savedPolygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    selectedPolygonId: String?
): List<PolygonVertexEntity> {
    return savedPolygons
        .find { it.first.id == selectedPolygonId }
        ?.second
        ?: emptyList()
}

suspend fun deleteSelectedVertexFromPolygon(
    db: AppDatabase,
    farmId: String,
    polygonId: String,
    vertexId: String,
    currentVertices: List<PolygonVertexEntity>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    val updatedVertices = currentVertices
        .sortedBy { it.vertexOrder }
        .filterNot { it.id == vertexId }
        .mapIndexed { index, vertex ->
            vertex.copy(vertexOrder = index)
        }

    withContext(Dispatchers.IO) {
        db.polygonDao().deleteVerticesByPolygonId(polygonId)
        db.polygonDao().insertVertices(updatedVertices)
    }

    return loadMapData(db, farmId).second
}

suspend fun persistPolygonVertices(
    db: AppDatabase,
    farmId: String,
    vertices: List<PolygonVertexEntity>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    withContext(Dispatchers.IO) {
        db.polygonDao().insertVertices(vertices.sortedBy { it.vertexOrder })
    }

    return loadMapData(db, farmId).second
}

suspend fun createPointAtLocation(
    db: AppDatabase,
    farmId: String,
    captureCategory: String,
    latitude: Double,
    longitude: Double
): Triple<String, String, List<MapPointEntity>> {
    return withContext(Dispatchers.IO) {
        val currentPoints = db.mapPointDao().getPointsByFarmId(farmId)
        val timestamp = System.currentTimeMillis()
        val pointId = "point_$timestamp"

        val nextPointName = buildNextPointName(
            category = captureCategory,
            existingPoints = currentPoints
        )

        db.mapPointDao().insert(
            MapPointEntity(
                id = pointId,
                farmId = farmId,
                name = nextPointName,
                description = null,
                category = captureCategory,
                latitude = latitude,
                longitude = longitude,
                createdAt = timestamp
            )
        )

        Triple(
            pointId,
            nextPointName,
            db.mapPointDao().getPointsByFarmId(farmId)
        )
    }
}

suspend fun savePolygon(
    db: AppDatabase,
    farmId: String,
    polygonName: String,
    polygonDescription: String,
    polygonVertices: List<LatLng>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        val polygonId = "polygon_${System.currentTimeMillis()}"
        val createdAt = System.currentTimeMillis()

        db.polygonDao().insertPolygon(
            PolygonEntity(
                id = polygonId,
                farmId = farmId,
                name = polygonName,
                description = polygonDescription.ifBlank { null },
                category = "General",
                createdAt = createdAt
            )
        )

        val vertices = polygonVertices.mapIndexed { index, vertex ->
            PolygonVertexEntity(
                id = "${polygonId}_vertex_$index",
                polygonId = polygonId,
                vertexOrder = index,
                latitude = vertex.latitude,
                longitude = vertex.longitude
            )
        }

        db.polygonDao().insertVertices(vertices)

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val loadedVertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to loadedVertices
        }
    }
}

suspend fun updatePointAttributes(
    db: AppDatabase,
    farmId: String,
    pointId: String,
    name: String,
    description: String,
    category: String
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().updatePointAttributes(
            pointId = pointId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun updatePointPosition(
    db: AppDatabase,
    farmId: String,
    pointId: String,
    latitude: Double,
    longitude: Double
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().updatePointPosition(
            pointId = pointId,
            latitude = latitude,
            longitude = longitude
        )
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun deletePointById(
    db: AppDatabase,
    farmId: String,
    pointId: String
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().deleteById(pointId)
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun deletePolygonById(
    db: AppDatabase,
    farmId: String,
    polygonId: String
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polygonDao().deletePolygonById(polygonId)

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val vertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to vertices
        }
    }
}

suspend fun updatePolygonAttributes(
    db: AppDatabase,
    farmId: String,
    polygonId: String,
    name: String,
    description: String,
    category: String
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polygonDao().updatePolygonAttributes(
            polygonId = polygonId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val vertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to vertices
        }
    }
}

suspend fun savePolyline(
    db: AppDatabase,
    farmId: String,
    polylineName: String,
    polylineDescription: String,
    polylineVertices: List<LatLng>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        val polylineId = "polyline_${System.currentTimeMillis()}"
        val createdAt = System.currentTimeMillis()

        db.polylineDao().insertPolyline(
            PolylineEntity(
                id = polylineId,
                farmId = farmId,
                name = polylineName,
                description = polylineDescription.ifBlank { null },
                category = "General",
                createdAt = createdAt
            )
        )

        val vertices = polylineVertices.mapIndexed { index, vertex ->
            PolylineVertexEntity(
                id = "${polylineId}_vertex_$index",
                polylineId = polylineId,
                vertexOrder = index,
                latitude = vertex.latitude,
                longitude = vertex.longitude
            )
        }

        db.polylineDao().insertVertices(vertices)

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val loadedVertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to loadedVertices
        }
    }
}

suspend fun deletePolylineById(
    db: AppDatabase,
    farmId: String,
    polylineId: String
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polylineDao().deletePolylineById(polylineId)

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }
    }
}

suspend fun updatePolylineAttributes(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    name: String,
    description: String,
    category: String
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polylineDao().updatePolylineAttributes(
            polylineId = polylineId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }
    }
}

fun getVerticesOfSelectedPolyline(
    savedPolylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>,
    selectedPolylineId: String?
): List<PolylineVertexEntity> {
    return savedPolylines
        .find { it.first.id == selectedPolylineId }
        ?.second
        ?: emptyList()
}

suspend fun persistPolylineVertices(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    vertices: List<PolylineVertexEntity>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    withContext(Dispatchers.IO) {
        db.polylineDao().deleteVerticesByPolylineId(polylineId)
        db.polylineDao().insertVertices(vertices.sortedBy { it.vertexOrder })
    }

    return loadPolylineData(db, farmId)
}

suspend fun deleteSelectedVertexFromPolyline(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    vertexId: String,
    currentVertices: List<PolylineVertexEntity>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    val updatedVertices = currentVertices
        .sortedBy { it.vertexOrder }
        .filterNot { it.id == vertexId }
        .mapIndexed { index, vertex ->
            vertex.copy(vertexOrder = index)
        }

    withContext(Dispatchers.IO) {
        db.polylineDao().deleteVerticesByPolylineId(polylineId)
        db.polylineDao().insertVertices(updatedVertices)
    }

    return loadPolylineData(db, farmId)
}

