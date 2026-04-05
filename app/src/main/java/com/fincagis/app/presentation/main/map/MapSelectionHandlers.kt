package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import com.fincagis.app.presentation.main.MapViewModel

sealed class MapSelectionCenterTarget {
    data class Point(val point: MapPointEntity) : MapSelectionCenterTarget()
    data class Polygon(val vertices: List<PolygonVertexEntity>) : MapSelectionCenterTarget()
    data class Polyline(val vertices: List<PolylineVertexEntity>) : MapSelectionCenterTarget()
    data object None : MapSelectionCenterTarget()
}

fun applySelectionResultState(
    mapViewModel: MapViewModel,
    selectionResult: MapSelectionResult
): MapSelectionCenterTarget {
    return when (selectionResult) {
        is MapSelectionResult.PointSelected -> {
            mapViewModel.selectPoint(selectionResult.point.id)
            MapSelectionCenterTarget.Point(selectionResult.point)
        }

        is MapSelectionResult.PolygonSelected -> {
            mapViewModel.selectPolygon(selectionResult.polygon.id)
            MapSelectionCenterTarget.Polygon(selectionResult.vertices)
        }

        is MapSelectionResult.PolylineSelected -> {
            mapViewModel.selectPolyline(selectionResult.polyline.id)
            MapSelectionCenterTarget.Polyline(selectionResult.vertices)
        }

        is MapSelectionResult.NothingSelected -> {
            mapViewModel.clearSelectionAndEditState()
            MapSelectionCenterTarget.None
        }
    }
}

fun handlePointListSelection(
    mapViewModel: MapViewModel,
    isSelected: Boolean,
    pointId: String
): Boolean {
    return if (isSelected) {
        mapViewModel.clearPointSelection()
        false
    } else {
        mapViewModel.selectPoint(pointId)
        true
    }
}

fun handlePolygonListSelection(
    mapViewModel: MapViewModel,
    isSelected: Boolean,
    polygonId: String
): Boolean {
    return if (isSelected) {
        mapViewModel.clearPolygonOnlySelection()
        false
    } else {
        mapViewModel.selectPolygon(polygonId)
        true
    }
}

fun handlePolylineListSelection(
    mapViewModel: MapViewModel,
    isSelected: Boolean,
    polylineId: String
): Boolean {
    return if (isSelected) {
        mapViewModel.clearPolylineOnlySelection()
        false
    } else {
        mapViewModel.selectPolyline(polylineId)
        true
    }
}
