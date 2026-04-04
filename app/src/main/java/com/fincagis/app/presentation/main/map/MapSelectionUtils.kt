package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import org.maplibre.android.maps.MapLibreMap
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

sealed class MapSelectionResult {
    data class PointSelected(
        val point: MapPointEntity,
        val status: String
    ) : MapSelectionResult()

    data class PolygonSelected(
        val polygon: PolygonEntity,
        val vertices: List<PolygonVertexEntity>,
        val status: String
    ) : MapSelectionResult()

    data class PolylineSelected(
        val polyline: PolylineEntity,
        val vertices: List<PolylineVertexEntity>,
        val status: String
    ) : MapSelectionResult()

    data class NothingSelected(
        val status: String
    ) : MapSelectionResult()
}

fun resolveMapSelection(
    capturedPoints: List<MapPointEntity>,
    savedPolygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    savedPolylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>,
    tapLatitude: Double,
    tapLongitude: Double,
    mapLibreMap: MapLibreMap?
): MapSelectionResult {
    val threshold = getSelectionThreshold(mapLibreMap)

    val tappedPoint = findNearestPointToTap(
        points = capturedPoints,
        tapLatitude = tapLatitude,
        tapLongitude = tapLongitude,
        map = mapLibreMap
    )

    val polygonInside = findPolygonContainingTap(
        polygons = savedPolygons,
        tapLat = tapLatitude,
        tapLng = tapLongitude
    )

    val polygonEdge = findNearestPolygonToTap(
        polygons = savedPolygons,
        tapLat = tapLatitude,
        tapLng = tapLongitude,
        threshold = threshold
    )

    val tappedPolygon = polygonInside ?: polygonEdge

    val tappedPolyline = findNearestPolylineToTap(
        polylines = savedPolylines,
        tapLat = tapLatitude,
        tapLng = tapLongitude,
        threshold = threshold
    )

    return when {
        tappedPoint != null -> {
            MapSelectionResult.PointSelected(
                point = tappedPoint,
                status = "Punto seleccionado: ${tappedPoint.name}"
            )
        }

        tappedPolyline != null -> {
            val vertices = savedPolylines
                .find { it.first.id == tappedPolyline.id }
                ?.second
                ?: emptyList()

            MapSelectionResult.PolylineSelected(
                polyline = tappedPolyline,
                vertices = vertices,
                status = "LÃ­nea seleccionada desde el mapa: ${tappedPolyline.name}"
            )
        }

        tappedPolygon != null -> {
            val vertices = savedPolygons
                .find { it.first.id == tappedPolygon.id }
                ?.second
                ?: emptyList()

            MapSelectionResult.PolygonSelected(
                polygon = tappedPolygon,
                vertices = vertices,
                status = "PolÃ­gono seleccionado desde el mapa: ${tappedPolygon.name}"
            )
        }

        else -> {
            MapSelectionResult.NothingSelected(
                status = "Nada seleccionado."
            )
        }
    }
}
