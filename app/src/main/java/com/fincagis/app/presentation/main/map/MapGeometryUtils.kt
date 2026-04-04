package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import org.maplibre.android.maps.MapLibreMap
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun distanceBetween(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val lat1Rad = Math.toRadians(lat1)
    val lon1Rad = Math.toRadians(lon1)
    val lat2Rad = Math.toRadians(lat2)
    val lon2Rad = Math.toRadians(lon2)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return EARTH_RADIUS_METERS * c
}

fun distancePointToSegment(
    px: Double,
    py: Double,
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
): Double {
    val refLatRad = Math.toRadians(px)
    val refLonRad = Math.toRadians(py)

    fun projectToLocalMeters(lat: Double, lon: Double): Pair<Double, Double> {
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)
        val localX = (lonRad - refLonRad) * cos(refLatRad) * EARTH_RADIUS_METERS
        val localY = (latRad - refLatRad) * EARTH_RADIUS_METERS
        return localX to localY
    }

    val (pxM, pyM) = projectToLocalMeters(px, py)
    val (x1M, y1M) = projectToLocalMeters(x1, y1)
    val (x2M, y2M) = projectToLocalMeters(x2, y2)

    val dx = x2M - x1M
    val dy = y2M - y1M

    if (dx == 0.0 && dy == 0.0) {
        return hypot(pxM - x1M, pyM - y1M)
    }

    val t = ((pxM - x1M) * dx + (pyM - y1M) * dy) / (dx * dx + dy * dy)
    val tClamped = t.coerceIn(0.0, 1.0)

    val closestX = x1M + tClamped * dx
    val closestY = y1M + tClamped * dy

    return hypot(pxM - closestX, pyM - closestY)
}

fun getSelectionThreshold(map: MapLibreMap?): Double {
    val zoom = map?.cameraPosition?.zoom ?: 16.0

    return when {
        zoom >= 18 -> 4.0
        zoom >= 16 -> 8.0
        zoom >= 14 -> 16.0
        zoom >= 12 -> 30.0
        else -> 60.0
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

fun findNearestPolylineSegmentToTap(
    vertices: List<PolylineVertexEntity>,
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

fun insertVertexIntoPolylineSegment(
    vertices: List<PolylineVertexEntity>,
    insertIndex: Int,
    lat: Double,
    lng: Double
): List<PolylineVertexEntity> {
    val ordered = vertices.sortedBy { it.vertexOrder }.toMutableList()

    val polylineId = ordered.firstOrNull()?.polylineId.orEmpty()

    val newVertex = PolylineVertexEntity(
        id = "vertex_${System.currentTimeMillis()}",
        polylineId = polylineId,
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
