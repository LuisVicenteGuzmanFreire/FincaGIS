package com.fincagis.app.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import com.fincagis.app.presentation.main.map.loadCompleteMapData
import com.fincagis.app.presentation.main.map.persistPolygonVertices
import com.fincagis.app.presentation.main.map.persistPolylineVertices
import com.fincagis.app.presentation.main.map.updatePointAttributes
import com.fincagis.app.presentation.main.map.updatePointPhotoReference
import com.fincagis.app.presentation.main.map.updatePointPosition
import com.fincagis.app.presentation.main.map.updatePolygonAttributes
import com.fincagis.app.presentation.main.map.updatePolylineAttributes

sealed class MapUndoAction {
    data class MovePoint(
        val pointId: String,
        val previousLatitude: Double,
        val previousLongitude: Double
    ) : MapUndoAction()

    data class MovePolygonVertex(
        val polygonId: String,
        val previousVertices: List<PolygonVertexEntity>
    ) : MapUndoAction()

    data class MovePolylineVertex(
        val polylineId: String,
        val previousVertices: List<PolylineVertexEntity>
    ) : MapUndoAction()
}

class MapViewModel(
    private val db: AppDatabase?,
    private val farmId: String
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var lastUndoAction: MapUndoAction? = null

    private fun setUndoAction(action: MapUndoAction?) {
        lastUndoAction = action
        uiState = uiState.copy(hasUndoAction = action != null)
    }

    suspend fun reloadCompleteMapData() {
        val safeDb = db ?: return
        if (farmId.isBlank()) return

        val data = loadCompleteMapData(
            db = safeDb,
            farmId = farmId
        )

        uiState = uiState.copy(
            capturedPoints = data.points,
            savedPolygons = data.polygons,
            savedPolylines = data.polylines
        )
    }

    fun setCapturedPoints(points: List<MapPointEntity>) {
        uiState = uiState.copy(capturedPoints = points)
    }

    fun setSavedPolygons(polygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>) {
        uiState = uiState.copy(savedPolygons = polygons)
    }

    fun setSavedPolylines(polylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>) {
        uiState = uiState.copy(savedPolylines = polylines)
    }

    fun registerPointMoveUndo(point: MapPointEntity) {
        setUndoAction(
            MapUndoAction.MovePoint(
                pointId = point.id,
                previousLatitude = point.latitude,
                previousLongitude = point.longitude
            )
        )
    }

    fun registerPolygonVertexMoveUndo(
        polygonId: String,
        vertices: List<PolygonVertexEntity>
    ) {
        setUndoAction(
            MapUndoAction.MovePolygonVertex(
                polygonId = polygonId,
                previousVertices = vertices.sortedBy { it.vertexOrder }
            )
        )
    }

    fun registerPolylineVertexMoveUndo(
        polylineId: String,
        vertices: List<PolylineVertexEntity>
    ) {
        setUndoAction(
            MapUndoAction.MovePolylineVertex(
                polylineId = polylineId,
                previousVertices = vertices.sortedBy { it.vertexOrder }
            )
        )
    }

    fun clearUndoAction() {
        setUndoAction(null)
    }

    suspend fun undoLastMovement() {
        val action = lastUndoAction ?: return
        val safeDb = db ?: return
        if (farmId.isBlank()) return

        when (action) {
            is MapUndoAction.MovePoint -> {
                uiState = uiState.copy(
                    capturedPoints = uiState.capturedPoints.map { point ->
                        if (point.id == action.pointId) {
                            point.copy(
                                latitude = action.previousLatitude,
                                longitude = action.previousLongitude
                            )
                        } else {
                            point
                        }
                    }
                )

                uiState = uiState.copy(
                    capturedPoints = updatePointPosition(
                        db = safeDb,
                        farmId = farmId,
                        pointId = action.pointId,
                        latitude = action.previousLatitude,
                        longitude = action.previousLongitude
                    )
                )
            }

            is MapUndoAction.MovePolygonVertex -> {
                uiState = uiState.copy(
                    savedPolygons = uiState.savedPolygons.map { (polygon, vertices) ->
                        if (polygon.id == action.polygonId) {
                            polygon to action.previousVertices
                        } else {
                            polygon to vertices
                        }
                    }
                )

                uiState = uiState.copy(
                    savedPolygons = persistPolygonVertices(
                        db = safeDb,
                        farmId = farmId,
                        vertices = action.previousVertices.sortedBy { it.vertexOrder }
                    )
                )
            }

            is MapUndoAction.MovePolylineVertex -> {
                uiState = uiState.copy(
                    savedPolylines = uiState.savedPolylines.map { (polyline, vertices) ->
                        if (polyline.id == action.polylineId) {
                            polyline to action.previousVertices
                        } else {
                            polyline to vertices
                        }
                    }
                )

                uiState = uiState.copy(
                    savedPolylines = persistPolylineVertices(
                        db = safeDb,
                        farmId = farmId,
                        polylineId = action.polylineId,
                        vertices = action.previousVertices.sortedBy { it.vertexOrder }
                    )
                )
            }
        }

        setUndoAction(null)
    }

    fun setSelectedPointId(pointId: String?) {
        uiState = uiState.copy(selectedPointId = pointId)
    }

    fun setPointEditMode(enabled: Boolean) {
        uiState = uiState.copy(
            isPointEditMode = enabled,
            isDraggingPoint = false
        )
    }

    fun startPointDrag() {
        uiState = uiState.copy(isDraggingPoint = true)
    }

    fun stopPointDrag() {
        uiState = uiState.copy(isDraggingPoint = false)
    }

    fun updatePointPositionInUi(pointId: String, latitude: Double, longitude: Double) {
        uiState = uiState.copy(
            capturedPoints = uiState.capturedPoints.map { point ->
                if (point.id == pointId) {
                    point.copy(
                        latitude = latitude,
                        longitude = longitude
                    )
                } else {
                    point
                }
            }
        )
    }

    suspend fun updateSelectedPointAttributes(
        name: String,
        description: String,
        category: String
    ): Boolean {
        val safeDb = db ?: return false
        if (farmId.isBlank()) return false
        val pointId = uiState.selectedPointId ?: return false

        uiState = uiState.copy(
            capturedPoints = updatePointAttributes(
                db = safeDb,
                farmId = farmId,
                pointId = pointId,
                name = name,
                description = description,
                category = category
            )
        )
        return true
    }

    suspend fun updatePointPhotoForPoint(
        pointId: String,
        photoPath: String,
        photoName: String,
        photoCapturedAt: Long,
        photoMimeType: String?,
        photoSizeBytes: Long?
    ): Boolean {
        val safeDb = db ?: return false
        if (farmId.isBlank()) return false

        uiState = uiState.copy(
            capturedPoints = updatePointPhotoReference(
                db = safeDb,
                farmId = farmId,
                pointId = pointId,
                photoPath = photoPath,
                photoName = photoName,
                photoCapturedAt = photoCapturedAt,
                photoMimeType = photoMimeType,
                photoSizeBytes = photoSizeBytes
            )
        )
        return true
    }

    suspend fun updateSelectedPolygonAttributes(
        name: String,
        description: String,
        category: String
    ): Boolean {
        val safeDb = db ?: return false
        if (farmId.isBlank()) return false
        val polygonId = uiState.selectedPolygonId ?: return false

        uiState = uiState.copy(
            savedPolygons = updatePolygonAttributes(
                db = safeDb,
                farmId = farmId,
                polygonId = polygonId,
                name = name,
                description = description,
                category = category
            )
        )
        return true
    }

    suspend fun updateSelectedPolylineAttributes(
        name: String,
        description: String,
        category: String
    ): Boolean {
        val safeDb = db ?: return false
        if (farmId.isBlank()) return false
        val polylineId = uiState.selectedPolylineId ?: return false

        uiState = uiState.copy(
            savedPolylines = updatePolylineAttributes(
                db = safeDb,
                farmId = farmId,
                polylineId = polylineId,
                name = name,
                description = description,
                category = category
            )
        )
        return true
    }

    fun setSelectedPolygonId(polygonId: String?) {
        uiState = uiState.copy(selectedPolygonId = polygonId)
    }

    fun setSelectedPolylineId(polylineId: String?) {
        uiState = uiState.copy(selectedPolylineId = polylineId)
    }

    fun setSelectedVertexId(vertexId: String?) {
        uiState = uiState.copy(selectedVertexId = vertexId)
    }

    fun setDraggingVertex(isDragging: Boolean) {
        uiState = uiState.copy(isDraggingVertex = isDragging)
    }

    fun setPolygonEditMode(enabled: Boolean) {
        uiState = uiState.copy(isPolygonEditMode = enabled)
    }

    fun setPolylineEditMode(enabled: Boolean) {
        uiState = uiState.copy(isPolylineEditMode = enabled)
    }

    fun clearVertexInteractionState() {
        uiState = uiState.copy(
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun clearSelectionAndEditState() {
        uiState = uiState.copy(
            selectedPointId = null,
            selectedPolygonId = null,
            selectedPolylineId = null,
            isPointEditMode = false,
            isDraggingPoint = false,
            isPolygonEditMode = false,
            isPolylineEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun selectPoint(pointId: String) {
        uiState = uiState.copy(
            selectedPointId = pointId,
            selectedPolygonId = null,
            selectedPolylineId = null,
            isPointEditMode = false,
            isDraggingPoint = false,
            isPolygonEditMode = false,
            isPolylineEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun selectPolygon(polygonId: String) {
        uiState = uiState.copy(
            selectedPointId = null,
            selectedPolygonId = polygonId,
            selectedPolylineId = null,
            isPointEditMode = false,
            isDraggingPoint = false,
            isPolygonEditMode = false,
            isPolylineEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun selectPolyline(polylineId: String) {
        uiState = uiState.copy(
            selectedPointId = null,
            selectedPolygonId = null,
            selectedPolylineId = polylineId,
            isPointEditMode = false,
            isDraggingPoint = false,
            isPolygonEditMode = false,
            isPolylineEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun clearPointSelection() {
        uiState = uiState.copy(
            selectedPointId = null,
            isPointEditMode = false,
            isDraggingPoint = false
        )
    }

    fun clearPolygonOnlySelection() {
        uiState = uiState.copy(selectedPolygonId = null)
    }

    fun clearPolylineOnlySelection() {
        uiState = uiState.copy(selectedPolylineId = null)
    }

    fun clearPolygonSelection() {
        uiState = uiState.copy(
            selectedPolygonId = null,
            isPointEditMode = false,
            isPolygonEditMode = false,
            isDraggingPoint = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun clearPolylineSelection() {
        uiState = uiState.copy(
            selectedPolylineId = null,
            isPointEditMode = false,
            isPolylineEditMode = false,
            isDraggingPoint = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun finishPolygonEdit() {
        uiState = uiState.copy(
            isPolygonEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }

    fun finishPolylineEdit() {
        uiState = uiState.copy(
            isPolylineEditMode = false,
            selectedVertexId = null,
            isDraggingVertex = false
        )
    }
}

class MapViewModelFactory(
    private val db: AppDatabase?,
    private val farmId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(db = db, farmId = farmId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
