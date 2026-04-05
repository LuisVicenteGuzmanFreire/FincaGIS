package com.fincagis.app.presentation.main

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

data class MapUiState(
    val capturedPoints: List<MapPointEntity> = emptyList(),
    val savedPolygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>> = emptyList(),
    val savedPolylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>> = emptyList(),
    val selectedPointId: String? = null,
    val selectedPolygonId: String? = null,
    val selectedPolylineId: String? = null,
    val isPointEditMode: Boolean = false,
    val isDraggingPoint: Boolean = false,
    val selectedVertexId: String? = null,
    val isDraggingVertex: Boolean = false,
    val isPolygonEditMode: Boolean = false,
    val isPolylineEditMode: Boolean = false,
    val hasUndoAction: Boolean = false
)
