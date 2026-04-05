package com.fincagis.app.presentation.main.map

import com.fincagis.app.presentation.main.MapViewModel

fun togglePointEditMode(mapViewModel: MapViewModel): Boolean {
    val isEnabled = !mapViewModel.uiState.isPointEditMode
    mapViewModel.setPointEditMode(isEnabled)
    if (isEnabled) {
        mapViewModel.setPolygonEditMode(false)
        mapViewModel.setPolylineEditMode(false)
        mapViewModel.clearVertexInteractionState()
    }
    return isEnabled
}

fun togglePolygonEditMode(mapViewModel: MapViewModel): Boolean {
    val isEnabled = !mapViewModel.uiState.isPolygonEditMode
    mapViewModel.setPolygonEditMode(isEnabled)
    if (isEnabled) {
        mapViewModel.setPointEditMode(false)
        mapViewModel.stopPointDrag()
    }
    mapViewModel.clearVertexInteractionState()
    return isEnabled
}

fun togglePolylineEditMode(mapViewModel: MapViewModel): Boolean {
    val isEnabled = !mapViewModel.uiState.isPolylineEditMode
    mapViewModel.setPolylineEditMode(isEnabled)
    mapViewModel.setPolygonEditMode(false)
    mapViewModel.setPointEditMode(false)
    mapViewModel.stopPointDrag()
    mapViewModel.clearVertexInteractionState()
    return isEnabled
}

fun clearAfterPolygonDelete(mapViewModel: MapViewModel) {
    mapViewModel.setSelectedPolygonId(null)
    mapViewModel.setPolygonEditMode(false)
    mapViewModel.setPolylineEditMode(false)
    mapViewModel.setPointEditMode(false)
    mapViewModel.stopPointDrag()
    mapViewModel.clearVertexInteractionState()
}

fun clearAfterPolygonDeselect(mapViewModel: MapViewModel) {
    clearAfterPolygonDelete(mapViewModel)
}
