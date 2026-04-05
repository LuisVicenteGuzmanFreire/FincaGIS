package com.fincagis.app.presentation.main.map

import androidx.compose.ui.graphics.Color
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon as GeoJsonPolygon
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

fun colorFromHex(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}

fun getPointColorByCategory(category: String): String {
    return when (category.trim()) {
        "Muestreo" -> "#2E7D32"
        "Referencia" -> "#1976D2"
        "Hallazgo" -> "#F57C00"
        "Infraestructura" -> "#7B1FA2"
        else -> "#455A64"
    }
}

fun addOrUpdateFarmMarker(
    style: Style,
    latitude: Double,
    longitude: Double
) {
    val feature = Feature.fromGeometry(Point.fromLngLat(longitude, latitude))
    val featureCollection = FeatureCollection.fromFeatures(arrayOf(feature))

    val sourceId = "farm-source"
    val layerId = "farm-circle-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleRadius(9f),
            circleColor("#D32F2F"),
            circleStrokeWidth(2f),
            circleStrokeColor("#FFFFFF"),
            circleOpacity(1f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateUserMarker(
    style: Style,
    latitude: Double,
    longitude: Double
) {
    val feature = Feature.fromGeometry(Point.fromLngLat(longitude, latitude))
    val featureCollection = FeatureCollection.fromFeatures(arrayOf(feature))

    val sourceId = "user-source"
    val layerId = "user-circle-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleRadius(10f),
            circleColor("#1976D2"),
            circleStrokeWidth(3f),
            circleStrokeColor("#FFFFFF"),
            circleOpacity(1f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateCapturedPoints(
    style: Style,
    points: List<MapPointEntity>,
    selectedPointId: String?,
    isPointEditMode: Boolean,
    isDraggingPoint: Boolean
) {
    val features = points.map { point ->
        val isSelected = point.id == selectedPointId
        val isSelectedEditing = isSelected && isPointEditMode
        val isSelectedDragging = isSelectedEditing && isDraggingPoint
        val baseColor = getPointColorByCategory(point.category)

        Feature.fromGeometry(
            Point.fromLngLat(point.longitude, point.latitude)
        ).also { feature ->
            feature.addStringProperty("pointColor", baseColor)
            feature.addNumberProperty(
                "pointRadius",
                when {
                    isSelectedDragging -> 14
                    isSelectedEditing -> 12
                    isSelected -> 10
                    else -> 7
                }
            )
            feature.addNumberProperty(
                "pointStrokeWidth",
                when {
                    isSelectedDragging -> 5
                    isSelectedEditing -> 4
                    isSelected -> 3
                    else -> 2
                }
            )
            feature.addStringProperty(
                "pointStrokeColor",
                when {
                    isSelectedDragging -> "#FFB300"
                    isSelectedEditing -> "#FDD835"
                    isSelected -> "#000000"
                    else -> "#FFFFFF"
                }
            )
            feature.addNumberProperty(
                "pointOpacity",
                if (isSelectedEditing) 1.0 else 0.92
            )
        }
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "captured-points-source"
    val layerId = "captured-points-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleColor(Expression.get("pointColor")),
            circleRadius(Expression.get("pointRadius")),
            circleStrokeWidth(Expression.get("pointStrokeWidth")),
            circleStrokeColor(Expression.get("pointStrokeColor")),
            circleOpacity(Expression.get("pointOpacity"))
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateTemporaryPolygonVertices(
    style: Style,
    vertices: List<LatLng>
) {
    val features = vertices.map { vertex ->
        Feature.fromGeometry(Point.fromLngLat(vertex.longitude, vertex.latitude))
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "temporary-polygon-vertices-source"
    val layerId = "temporary-polygon-vertices-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleRadius(5.5f),
            circleColor("#8E24AA"),
            circleStrokeWidth(1.8f),
            circleStrokeColor("#FFFFFF"),
            circleOpacity(0.9f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateTemporaryPolygonLine(
    style: Style,
    vertices: List<LatLng>
) {
    val sourceId = "temporary-polygon-line-source"
    val layerId = "temporary-polygon-line-layer"

    val featureCollection = if (vertices.size >= 2) {
        val points = vertices.map { vertex ->
            Point.fromLngLat(vertex.longitude, vertex.latitude)
        }

        FeatureCollection.fromFeatures(
            arrayOf(Feature.fromGeometry(LineString.fromLngLats(points)))
        )
    } else {
        FeatureCollection.fromFeatures(emptyArray())
    }

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = LineLayer(layerId, sourceId).withProperties(
            lineColor("#8E24AA"),
            lineWidth(3f),
            lineOpacity(0.9f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateSavedPolygons(
    style: Style,
    polygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    selectedPolygonId: String?,
    isPolygonEditMode: Boolean
) {
    val features = polygons.mapNotNull { (polygon, vertices) ->
        if (vertices.size < 3) {
            null
        } else {
            val ring = vertices
                .sortedBy { it.vertexOrder }
                .map { Point.fromLngLat(it.longitude, it.latitude) }
                .toMutableList()

            val first = ring.first()
            val last = ring.last()

            if (first.longitude() != last.longitude() || first.latitude() != last.latitude()) {
                ring.add(first)
            }

            val isSelected = polygon.id == selectedPolygonId
            val isEditing = isSelected && isPolygonEditMode

            Feature.fromGeometry(
                GeoJsonPolygon.fromLngLats(listOf(ring))
            ).also { feature ->
                feature.addStringProperty(
                    "polygonFillColor",
                    when {
                        isEditing -> "#7B1FA2"
                        isSelected -> "#AB47BC"
                        else -> "#8E24AA"
                    }
                )
                feature.addNumberProperty(
                    "polygonFillOpacity",
                    when {
                        isEditing -> 0.45
                        isSelected -> 0.34
                        else -> 0.20
                    }
                )
                feature.addStringProperty(
                    "polygonLineColor",
                    when {
                        isEditing -> "#311B92"
                        isSelected -> "#4A148C"
                        else -> "#6A1B9A"
                    }
                )
                feature.addNumberProperty(
                    "polygonLineWidth",
                    when {
                        isEditing -> 6.5
                        isSelected -> 4.5
                        else -> 2.5
                    }
                )
            }
        }
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "saved-polygons-source"
    val fillLayerId = "saved-polygons-fill-layer"
    val lineLayerId = "saved-polygons-line-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(fillLayerId) == null) {
        val fillLayer = FillLayer(fillLayerId, sourceId).withProperties(
            fillColor(Expression.get("polygonFillColor")),
            fillOpacity(Expression.get("polygonFillOpacity"))
        )
        style.addLayer(fillLayer)
    }

    if (style.getLayer(lineLayerId) == null) {
        val lineLayer = LineLayer(lineLayerId, sourceId).withProperties(
            lineColor(Expression.get("polygonLineColor")),
            lineWidth(Expression.get("polygonLineWidth"))
        )
        style.addLayer(lineLayer)
    }
}

fun addOrUpdatePolygonVerticesLayer(
    style: Style,
    vertices: List<PolygonVertexEntity>,
    selectedVertexId: String?,
    isDraggingVertex: Boolean
) {
    val features = vertices.map { vertex ->
        Feature.fromGeometry(
            Point.fromLngLat(vertex.longitude, vertex.latitude)
        ).also { feature ->
            val isSelected = vertex.id == selectedVertexId
            val isSelectedDragging = isSelected && isDraggingVertex

            feature.addStringProperty(
                "vertexColor",
                if (isSelected) "#E53935" else "#1E88E5"
            )
            feature.addNumberProperty(
                "vertexRadius",
                if (isSelectedDragging) 12.0 else if (isSelected) 10.0 else 5.5
            )
            feature.addStringProperty(
                "vertexStrokeColor",
                if (isSelectedDragging) "#FDD835" else "#FFFFFF"
            )
            feature.addNumberProperty(
                "vertexStrokeWidth",
                if (isSelectedDragging) 3.0 else if (isSelected) 2.4 else 1.5
            )
        }
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "polygon-vertices-source"
    val layerId = "polygon-vertices-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleColor(Expression.get("vertexColor")),
            circleRadius(Expression.get("vertexRadius")),
            circleStrokeColor(Expression.get("vertexStrokeColor")),
            circleStrokeWidth(Expression.get("vertexStrokeWidth"))
        )
        style.addLayer(layer)
    }
}

fun addOrUpdatePolylineVerticesLayer(
    style: Style,
    vertices: List<PolylineVertexEntity>,
    selectedVertexId: String?,
    isDraggingVertex: Boolean
) {
    val features = vertices.map { vertex ->
        Feature.fromGeometry(
            Point.fromLngLat(vertex.longitude, vertex.latitude)
        ).also { feature ->
            val isSelected = vertex.id == selectedVertexId
            val isSelectedDragging = isSelected && isDraggingVertex

            feature.addStringProperty(
                "vertexColor",
                if (isSelected) "#E53935" else "#1E88E5"
            )
            feature.addNumberProperty(
                "vertexRadius",
                if (isSelectedDragging) 12.0 else if (isSelected) 10.0 else 5.5
            )
            feature.addStringProperty(
                "vertexStrokeColor",
                if (isSelectedDragging) "#FDD835" else "#FFFFFF"
            )
            feature.addNumberProperty(
                "vertexStrokeWidth",
                if (isSelectedDragging) 3.0 else if (isSelected) 2.4 else 1.5
            )
        }
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "polyline-vertices-source"
    val layerId = "polyline-vertices-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleColor(Expression.get("vertexColor")),
            circleRadius(Expression.get("vertexRadius")),
            circleStrokeColor(Expression.get("vertexStrokeColor")),
            circleStrokeWidth(Expression.get("vertexStrokeWidth"))
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateTemporaryPolyline(
    style: Style,
    vertices: List<LatLng>
) {
    val sourceId = "temporary-polyline-source"
    val layerId = "temporary-polyline-layer"

    val featureCollection = if (vertices.size >= 2) {
        val points = vertices.map { vertex ->
            Point.fromLngLat(vertex.longitude, vertex.latitude)
        }

        FeatureCollection.fromFeatures(
            arrayOf(Feature.fromGeometry(LineString.fromLngLats(points)))
        )
    } else {
        FeatureCollection.fromFeatures(emptyArray())
    }

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = LineLayer(layerId, sourceId).withProperties(
            lineColor("#00796B"),
            lineWidth(3.5f),
            lineOpacity(0.9f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateTemporaryPolylineVertices(
    style: Style,
    vertices: List<LatLng>
) {
    val features = vertices.map { vertex ->
        Feature.fromGeometry(Point.fromLngLat(vertex.longitude, vertex.latitude))
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "temporary-polyline-vertices-source"
    val layerId = "temporary-polyline-vertices-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = CircleLayer(layerId, sourceId).withProperties(
            circleRadius(5.5f),
            circleColor("#00796B"),
            circleStrokeWidth(1.8f),
            circleStrokeColor("#FFFFFF"),
            circleOpacity(0.9f)
        )
        style.addLayer(layer)
    }
}

fun addOrUpdateSavedPolylines(
    style: Style,
    polylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>,
    selectedPolylineId: String?,
    isPolylineEditMode: Boolean
) {
    val features = polylines.mapNotNull { (polyline, vertices) ->
        if (vertices.size < 2) {
            null
        } else {
            val points = vertices
                .sortedBy { it.vertexOrder }
                .map { Point.fromLngLat(it.longitude, it.latitude) }

            val isSelected = polyline.id == selectedPolylineId
            val isEditing = isSelected && isPolylineEditMode

            Feature.fromGeometry(
                LineString.fromLngLats(points)
            ).also { feature ->
                feature.addStringProperty(
                    "polylineColor",
                    when {
                        isEditing -> "#00695C"
                        isSelected -> "#004D40"
                        else -> "#00897B"
                    }
                )
                feature.addNumberProperty(
                    "polylineWidth",
                    when {
                        isEditing -> 8.5
                        isSelected -> 6.0
                        else -> 4.0
                    }
                )
                feature.addNumberProperty(
                    "polylineOpacity",
                    if (isEditing) 1.0 else 0.95
                )
            }
        }
    }

    val featureCollection = FeatureCollection.fromFeatures(features)
    val sourceId = "saved-polylines-source"
    val layerId = "saved-polylines-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(featureCollection)
    } else {
        style.addSource(GeoJsonSource(sourceId, featureCollection))
    }

    if (style.getLayer(layerId) == null) {
        val layer = LineLayer(layerId, sourceId).withProperties(
            lineColor(Expression.get("polylineColor")),
            lineWidth(Expression.get("polylineWidth")),
            lineOpacity(Expression.get("polylineOpacity"))
        )
        style.addLayer(layer)
    }
}
