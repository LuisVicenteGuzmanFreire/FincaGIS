package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import org.maplibre.android.maps.MapLibreMap
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

fun distanceBetween(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val dLat = lat1 - lat2
    val dLon = lon1 - lon2
    return kotlin.math.sqrt(dLat * dLat + dLon * dLon)
}

fun distancePointToSegment(
    px: Double,
    py: Double,
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
): Double {
    val dx = x2 - x1
    val dy = y2 - y1

    if (dx == 0.0 && dy == 0.0) {
        return distanceBetween(px, py, x1, y1)
    }

    val t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)
    val tClamped = t.coerceIn(0.0, 1.0)

    val closestX = x1 + tClamped * dx
    val closestY = y1 + tClamped * dy

    return distanceBetween(px, py, closestX, closestY)
}

fun getSelectionThreshold(map: MapLibreMap?): Double {
    val zoom = map?.cameraPosition?.zoom ?: 16.0

    return when {
        zoom >= 18 -> 0.00002
        zoom >= 16 -> 0.00005
        zoom >= 14 -> 0.0001
        zoom >= 12 -> 0.0002
        else -> 0.0004
    }
}

fun findNearestPointToTap(
    points: List<MapPointEntity>,
    tapLatitude: Double,
    tapLongitude: Double,
    map: MapLibreMap?
): MapPointEntity? {
    val threshold = getSelectionThreshold(map)

    return points
        .map { point ->
            point to distanceBetween(
                lat1 = point.latitude,
                lon1 = point.longitude,
                lat2 = tapLatitude,
                lon2 = tapLongitude
            )
        }
        .filter { (_, distance) -> distance < threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}

fun findNearestVertexToTap(
    vertices: List<PolygonVertexEntity>,
    tapLat: Double,
    tapLng: Double,
    threshold: Double
): PolygonVertexEntity? {
    return vertices
        .map { vertex ->
            vertex to distanceBetween(
                lat1 = vertex.latitude,
                lon1 = vertex.longitude,
                lat2 = tapLat,
                lon2 = tapLng
            )
        }
        .filter { (_, distance) -> distance < threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}

fun findNearestPolylineVertexToTap(
    vertices: List<PolylineVertexEntity>,
    tapLat: Double,
    tapLng: Double,
    threshold: Double
): PolylineVertexEntity? {
    return vertices
        .map { vertex ->
            vertex to distanceBetween(
                lat1 = vertex.latitude,
                lon1 = vertex.longitude,
                lat2 = tapLat,
                lon2 = tapLng
            )
        }
        .filter { (_, distance) -> distance < threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}

fun isPointInsidePolygon(
    tapLat: Double,
    tapLng: Double,
    vertices: List<PolygonVertexEntity>
): Boolean {
    if (vertices.size < 3) return false

    val ordered = vertices.sortedBy { it.vertexOrder }
    var inside = false
    var j = ordered.size - 1

    for (i in ordered.indices) {
        val xi = ordered[i].latitude
        val yi = ordered[i].longitude
        val xj = ordered[j].latitude
        val yj = ordered[j].longitude

        val intersect =
            ((yi > tapLng) != (yj > tapLng)) &&
                    (tapLat < (xj - xi) * (tapLng - yi) / (yj - yi + 0.0000001) + xi)

        if (intersect) inside = !inside
        j = i
    }

    return inside
}

fun findPolygonContainingTap(
    polygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    tapLat: Double,
    tapLng: Double
): PolygonEntity? {
    return polygons.firstOrNull { (_, vertices) ->
        isPointInsidePolygon(tapLat, tapLng, vertices)
    }?.first
}

fun findNearestPolygonToTap(
    polygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    tapLat: Double,
    tapLng: Double,
    threshold: Double
): PolygonEntity? {
    return polygons.mapNotNull { (polygon, vertices) ->
        if (vertices.size < 2) return@mapNotNull null

        val ordered = vertices.sortedBy { it.vertexOrder }

        val distances = ordered.zipWithNext { v1, v2 ->
            distancePointToSegment(
                px = tapLat,
                py = tapLng,
                x1 = v1.latitude,
                y1 = v1.longitude,
                x2 = v2.latitude,
                y2 = v2.longitude
            )
        }

        val minDistance = distances.minOrNull() ?: return@mapNotNull null
        polygon to minDistance
    }
        .filter { (_, distance) -> distance < threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}

fun findNearestSegmentToTap(
    vertices: List<PolygonVertexEntity>,
    tapLat: Double,
    tapLng: Double,
    threshold: Double
): Pair<Int, Int>? {
    if (vertices.size < 2) return null

    val ordered = vertices.sortedBy { it.vertexOrder }

    val candidates = ordered.zipWithNext().mapIndexed { index, (v1, v2) ->
        val distance = distancePointToSegment(
            px = tapLat,
            py = tapLng,
            x1 = v1.latitude,
            y1 = v1.longitude,
            x2 = v2.latitude,
            y2 = v2.longitude
        )
        Triple(index, index + 1, distance)
    }

    val best = candidates
        .filter { it.third < threshold }
        .minByOrNull { it.third }

    return best?.let { it.first to it.second }
}

fun insertVertexIntoSegment(
    vertices: List<PolygonVertexEntity>,
    insertIndex: Int,
    lat: Double,
    lng: Double
): List<PolygonVertexEntity> {
    val ordered = vertices.sortedBy { it.vertexOrder }.toMutableList()

    val polygonId = ordered.firstOrNull()?.polygonId.orEmpty()

    val newVertex = PolygonVertexEntity(
        id = "vertex_${System.currentTimeMillis()}",
        polygonId = polygonId,
        vertexOrder = insertIndex + 1,
        latitude = lat,
        longitude = lng
    )

    ordered.add(insertIndex + 1, newVertex)

    return ordered.mapIndexed { index, vertex ->
        vertex.copy(vertexOrder = index)
    }
}

fun findNearestPolylineToTap(
    polylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>,
    tapLat: Double,
    tapLng: Double,
    threshold: Double
): PolylineEntity? {
    return polylines.mapNotNull { (polyline, vertices) ->
        if (vertices.size < 2) return@mapNotNull null

        val ordered = vertices.sortedBy { it.vertexOrder }

        val distances = ordered.zipWithNext { v1, v2 ->
            distancePointToSegment(
                px = tapLat,
                py = tapLng,
                x1 = v1.latitude,
                y1 = v1.longitude,
                x2 = v2.latitude,
                y2 = v2.longitude
            )
        }

        val minDistance = distances.minOrNull() ?: return@mapNotNull null
        polyline to minDistance
    }
        .filter { (_, distance) -> distance < threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}