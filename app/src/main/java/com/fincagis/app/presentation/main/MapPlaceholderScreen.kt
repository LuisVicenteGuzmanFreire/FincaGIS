package com.fincagis.app.presentation.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.core.ui.AppTopBar
import com.fincagis.app.core.ui.theme.FincagisTheme
import com.fincagis.app.data.local.entity.MapPointEntity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.ClipboardManager
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import android.graphics.PointF
import android.view.MotionEvent

//Import Helpers map
import com.fincagis.app.presentation.main.map.addOrUpdateCapturedPoints
import com.fincagis.app.presentation.main.map.addOrUpdateFarmMarker
import com.fincagis.app.presentation.main.map.addOrUpdatePolygonVerticesLayer
import com.fincagis.app.presentation.main.map.addOrUpdatePolylineVerticesLayer
import com.fincagis.app.presentation.main.map.addOrUpdateSavedPolygons
import com.fincagis.app.presentation.main.map.addOrUpdateTemporaryPolygonLine
import com.fincagis.app.presentation.main.map.addOrUpdateTemporaryPolygonVertices
import com.fincagis.app.presentation.main.map.addOrUpdateUserMarker
import com.fincagis.app.presentation.main.map.buildAutoNameForEditedPoint
import com.fincagis.app.presentation.main.map.buildNextPolygonName
import com.fincagis.app.presentation.main.map.colorFromHex
import com.fincagis.app.presentation.main.map.findNearestVertexToTap
import com.fincagis.app.presentation.main.map.findNearestSegmentToTap
import com.fincagis.app.presentation.main.map.insertVertexIntoSegment
import com.fincagis.app.presentation.main.map.findNearestPointToTap
import com.fincagis.app.presentation.main.map.togglePointEditMode
import com.fincagis.app.presentation.main.map.togglePolygonEditMode
import com.fincagis.app.presentation.main.map.togglePolylineEditMode
import com.fincagis.app.presentation.main.map.clearAfterPolygonDelete
import com.fincagis.app.presentation.main.map.clearAfterPolygonDeselect
import com.fincagis.app.presentation.main.map.formatCoordinate
import com.fincagis.app.presentation.main.map.formatPointDescription
import com.fincagis.app.presentation.main.map.formatTimestamp
import com.fincagis.app.presentation.main.map.getPointColorByCategory
import com.fincagis.app.presentation.main.map.getSelectionThreshold
import com.fincagis.app.presentation.main.map.isAutoGeneratedPointName
import com.fincagis.app.presentation.main.map.centerOnBoth
import com.fincagis.app.presentation.main.map.centerOnFarm
import com.fincagis.app.presentation.main.map.centerOnSelectedPoint
import com.fincagis.app.presentation.main.map.centerOnSelectedPolygon
import com.fincagis.app.presentation.main.map.centerOnSelectedPolyline
import com.fincagis.app.presentation.main.map.centerOnUser
import com.fincagis.app.presentation.main.map.createPointAtLocation
import com.fincagis.app.presentation.main.map.deletePointById
import com.fincagis.app.presentation.main.map.deletePolygonById
import com.fincagis.app.presentation.main.map.deleteSelectedVertexFromPolygon
import com.fincagis.app.presentation.main.map.getVerticesOfSelectedPolygon
import com.fincagis.app.presentation.main.map.persistPolygonVertices
import com.fincagis.app.presentation.main.map.savePolygon
import com.fincagis.app.presentation.main.map.updatePointPosition
import com.fincagis.app.presentation.main.map.MapSelectionResult
import com.fincagis.app.presentation.main.map.MapSelectionCenterTarget
import com.fincagis.app.presentation.main.map.resolveMapSelection
import com.fincagis.app.presentation.main.map.applySelectionResultState
import com.fincagis.app.presentation.main.map.handlePointListSelection
import com.fincagis.app.presentation.main.map.handlePolygonListSelection
import com.fincagis.app.presentation.main.map.handlePolylineListSelection
import com.fincagis.app.presentation.main.map.addOrUpdateSavedPolylines
import com.fincagis.app.presentation.main.map.addOrUpdateTemporaryPolyline
import com.fincagis.app.presentation.main.map.buildNextPolylineName
import com.fincagis.app.presentation.main.map.deletePolylineById
import com.fincagis.app.presentation.main.map.savePolyline
import com.fincagis.app.presentation.main.map.deleteSelectedVertexFromPolyline
import com.fincagis.app.presentation.main.map.getVerticesOfSelectedPolyline
import com.fincagis.app.presentation.main.map.persistPolylineVertices
import com.fincagis.app.presentation.main.map.findNearestPolylineVertexToTap
import com.fincagis.app.presentation.main.map.findNearestPolylineSegmentToTap
import com.fincagis.app.presentation.main.map.insertVertexIntoPolylineSegment


@Composable
fun MapPlaceholderScreen(
    db: AppDatabase? = null,
    farmId: String = "",
    farmName: String,
    latitude: Double,
    longitude: Double,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mapViewModel: MapViewModel = viewModel(
        key = "map-$farmId",
        factory = MapViewModelFactory(
            db = db,
            farmId = farmId
        )
    )
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var locationStatus by remember { mutableStateOf("Ubicación del dispositivo no disponible.") }
    var captureStatus by remember { mutableStateOf("Modo selección activo.") }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isCaptureModeEnabled by remember { mutableStateOf(false) }
    var isPolygonCaptureModeEnabled by remember { mutableStateOf(false) }
    var isPolylineCaptureModeEnabled by remember { mutableStateOf(false) }
    var captureCategory by remember { mutableStateOf("Muestreo") }
    var polygonVertices by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var polygonNameInput by remember { mutableStateOf("") }
    var polygonDescriptionInput by remember { mutableStateOf("") }
    var polylineVertices by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var polylineNameInput by remember { mutableStateOf("") }
    var polylineDescriptionInput by remember { mutableStateOf("") }
    var pointNameInput by remember { mutableStateOf("") }
    var pointDescriptionInput by remember { mutableStateOf("") }
    var pointCategoryInput by remember { mutableStateOf("Muestreo") }
    var selectedPolygonNameInput by remember { mutableStateOf("") }
    var selectedPolygonDescriptionInput by remember { mutableStateOf("") }
    var selectedPolygonCategoryInput by remember { mutableStateOf("General") }
    var selectedPolylineNameInput by remember { mutableStateOf("") }
    var selectedPolylineDescriptionInput by remember { mutableStateOf("") }
    var selectedPolylineCategoryInput by remember { mutableStateOf("General") }
    val uiState = mapViewModel.uiState
    val capturedPoints = uiState.capturedPoints
    val savedPolygons = uiState.savedPolygons
    val savedPolylines = uiState.savedPolylines
    val selectedPointId = uiState.selectedPointId
    val selectedPolygonId = uiState.selectedPolygonId
    val selectedPolylineId = uiState.selectedPolylineId
    val isPointEditMode = uiState.isPointEditMode
    val isDraggingPoint = uiState.isDraggingPoint
    val selectedVertexId = uiState.selectedVertexId
    val isDraggingVertex = uiState.isDraggingVertex
    val isPolygonEditMode = uiState.isPolygonEditMode
    val isPolylineEditMode = uiState.isPolylineEditMode
    val hasUndoAction = uiState.hasUndoAction
    val selectedPoint = uiState.capturedPoints.find { it.id == uiState.selectedPointId }
    val pointCategories = listOf(
        "Muestreo",
        "Referencia",
        "Hallazgo",
        "Infraestructura"
    )

    val selectedPolygon = uiState.savedPolygons.find { it.first.id == uiState.selectedPolygonId }
    val selectedPolyline = uiState.savedPolylines.find { it.first.id == uiState.selectedPolylineId }

    LaunchedEffect(selectedPointId, capturedPoints) {
        val point = capturedPoints.find { it.id == selectedPointId }

        if (point != null) {
            pointNameInput = point.name
            pointDescriptionInput = point.description.orEmpty()
            pointCategoryInput = point.category
        } else {
            pointNameInput = ""
            pointDescriptionInput = ""
            pointCategoryInput = "Muestreo"
        }
    }

    LaunchedEffect(selectedPolygonId, savedPolygons) {
        val polygon = savedPolygons.find { it.first.id == selectedPolygonId }?.first
        if (polygon != null) {
            selectedPolygonNameInput = polygon.name
            selectedPolygonDescriptionInput = polygon.description.orEmpty()
            selectedPolygonCategoryInput = polygon.category
        } else {
            selectedPolygonNameInput = ""
            selectedPolygonDescriptionInput = ""
            selectedPolygonCategoryInput = "General"
        }
    }

    LaunchedEffect(selectedPolylineId, savedPolylines) {
        val polyline = savedPolylines.find { it.first.id == selectedPolylineId }?.first
        if (polyline != null) {
            selectedPolylineNameInput = polyline.name
            selectedPolylineDescriptionInput = polyline.description.orEmpty()
            selectedPolylineCategoryInput = polyline.category
        } else {
            selectedPolylineNameInput = ""
            selectedPolylineDescriptionInput = ""
            selectedPolylineCategoryInput = "General"
        }
    }

    fun copySelectedPointCoordinates(point: MapPointEntity) {
        val coordinatesText =
            "${formatCoordinate(point.latitude)}, ${formatCoordinate(point.longitude)}"

        val clip = ClipData.newPlainText(
            "Coordenadas del punto",
            coordinatesText
        )
        clipboardManager.setPrimaryClip(clip)
        captureStatus = "Coordenadas copiadas: $coordinatesText"
    }

    fun loadCurrentLocation() {
        if (!hasLocationPermission(context)) {
            locationStatus = "Permiso de ubicación no concedido."
            return
        }

        try {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            )
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        locationStatus = "Ubicación obtenida correctamente."
                    } else {
                        locationStatus = "Ubicación recibida nula."
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                context.mainLooper
            )
        } catch (_: SecurityException) {
            locationStatus = "Error de permisos de ubicación."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (grantedFine || grantedCoarse) {
            loadCurrentLocation()
        } else {
            locationStatus = "Permiso de ubicación denegado."
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            loadCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(db, farmId) {
        mapViewModel.reloadCompleteMapData()
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())

            setOnTouchListener { view, event ->
                val touchUiState = mapViewModel.uiState
                val capturedPoints = touchUiState.capturedPoints
                val savedPolygons = touchUiState.savedPolygons
                val savedPolylines = touchUiState.savedPolylines
                val selectedPointId = touchUiState.selectedPointId
                val selectedPolygonId = touchUiState.selectedPolygonId
                val selectedPolylineId = touchUiState.selectedPolylineId
                val isPointEditMode = touchUiState.isPointEditMode
                val isDraggingPoint = touchUiState.isDraggingPoint
                val selectedVertexId = touchUiState.selectedVertexId
                val isDraggingVertex = touchUiState.isDraggingVertex
                val isPolygonEditMode = touchUiState.isPolygonEditMode
                val isPolylineEditMode = touchUiState.isPolylineEditMode
                val isEditingPoint = isPointEditMode && selectedPointId != null
                val isEditingPolygon = isPolygonEditMode && selectedPolygonId != null
                val isEditingPolyline = isPolylineEditMode && selectedPolylineId != null

                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                } else if (
                    (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) &&
                    !isDraggingVertex &&
                    !isDraggingPoint
                ) {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                }

                if (
                    (
                        !isEditingPoint &&
                        !isEditingPolygon &&
                            !isEditingPolyline
                        ) ||
                    db == null ||
                    farmId.isBlank()
                ) {
                    return@setOnTouchListener false
                }

                val map = mapLibreMap ?: return@setOnTouchListener false
                val polygonVerticesForEdit = getVerticesOfSelectedPolygon(
                    savedPolygons = savedPolygons,
                    selectedPolygonId = selectedPolygonId
                )

                val polylineVerticesForEdit = getVerticesOfSelectedPolyline(
                    savedPolylines = savedPolylines,
                    selectedPolylineId = selectedPolylineId
                )

                val threshold = getSelectionThreshold(mapLibreMap) * 1.8

                when (event.actionMasked) {

                    MotionEvent.ACTION_DOWN -> {
                        val downLatLng = map.projection.fromScreenLocation(PointF(event.x, event.y))
                        if (downLatLng == null) {
                            return@setOnTouchListener false
                        }

                        if (isEditingPoint) {
                            val selectedPointForEdit = capturedPoints.find { it.id == selectedPointId }
                            if (selectedPointForEdit != null) {
                                val tappedPoint = findNearestPointToTap(
                                    points = listOf(selectedPointForEdit),
                                    tapLatitude = downLatLng.latitude,
                                    tapLongitude = downLatLng.longitude,
                                    map = mapLibreMap
                                )

                                if (tappedPoint != null) {
                                    mapViewModel.registerPointMoveUndo(selectedPointForEdit)
                                    mapViewModel.startPointDrag()
                                    captureStatus = "Arrastrando punto..."
                                    view.parent?.requestDisallowInterceptTouchEvent(true)
                                    return@setOnTouchListener true
                                }
                            }
                        } else if (isEditingPolygon) {
                            val tappedVertex = findNearestVertexToTap(
                                vertices = polygonVerticesForEdit,
                                tapLat = downLatLng.latitude,
                                tapLng = downLatLng.longitude,
                                threshold = threshold
                            )

                            if (tappedVertex != null) {
                                selectedPolygonId?.let { polygonId ->
                                    mapViewModel.registerPolygonVertexMoveUndo(
                                        polygonId = polygonId,
                                        vertices = polygonVerticesForEdit
                                    )
                                }
                                mapViewModel.setSelectedVertexId(tappedVertex.id)
                                mapViewModel.setDraggingVertex(true)
                                captureStatus = "Arrastrando vértice..."
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                return@setOnTouchListener true
                            }

                            val tappedSegment = findNearestSegmentToTap(
                                vertices = polygonVerticesForEdit,
                                tapLat = downLatLng.latitude,
                                tapLng = downLatLng.longitude,
                                threshold = threshold
                            )

                            if (tappedSegment != null && db != null && selectedPolygonId != null) {
                                val insertIndex = tappedSegment.first
                                val updatedVertices = insertVertexIntoSegment(
                                    vertices = polygonVerticesForEdit,
                                    insertIndex = insertIndex,
                                    lat = downLatLng.latitude,
                                    lng = downLatLng.longitude
                                )

                                val insertedVertex = updatedVertices.getOrNull(insertIndex + 1)

                                mapViewModel.setSavedPolygons(savedPolygons.map { (poly, verts) ->
                                    if (poly.id == selectedPolygonId) {
                                        poly to updatedVertices
                                    } else {
                                        poly to verts
                                    }
                                })

                                mapViewModel.setSelectedVertexId(insertedVertex?.id)
                                captureStatus = "Vértice insertado en polígono."

                                coroutineScope.launch {
                                    mapViewModel.setSavedPolygons(
                                        persistPolygonVertices(
                                            db = db,
                                            farmId = farmId,
                                            vertices = updatedVertices.sortedBy { it.vertexOrder }
                                        )
                                    )
                                }

                                return@setOnTouchListener true
                            }
                        } else if (isEditingPolyline) {
                            val tappedVertex = findNearestPolylineVertexToTap(
                                vertices = polylineVerticesForEdit,
                                tapLat = downLatLng.latitude,
                                tapLng = downLatLng.longitude,
                                threshold = threshold
                            )

                            if (tappedVertex != null) {
                                selectedPolylineId?.let { polylineId ->
                                    mapViewModel.registerPolylineVertexMoveUndo(
                                        polylineId = polylineId,
                                        vertices = polylineVerticesForEdit
                                    )
                                }
                                mapViewModel.setSelectedVertexId(tappedVertex.id)
                                mapViewModel.setDraggingVertex(true)
                                captureStatus = "Arrastrando vértice..."
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                return@setOnTouchListener true
                            }

                            val tappedSegment = findNearestPolylineSegmentToTap(
                                vertices = polylineVerticesForEdit,
                                tapLat = downLatLng.latitude,
                                tapLng = downLatLng.longitude,
                                threshold = threshold
                            )

                            if (tappedSegment != null && db != null && selectedPolylineId != null) {
                                val insertIndex = tappedSegment.first
                                val updatedVertices = insertVertexIntoPolylineSegment(
                                    vertices = polylineVerticesForEdit,
                                    insertIndex = insertIndex,
                                    lat = downLatLng.latitude,
                                    lng = downLatLng.longitude
                                )

                                val insertedVertex = updatedVertices.getOrNull(insertIndex + 1)

                                mapViewModel.setSavedPolylines(savedPolylines.map { (line, verts) ->
                                    if (line.id == selectedPolylineId) {
                                        line to updatedVertices
                                    } else {
                                        line to verts
                                    }
                                })

                                mapViewModel.setSelectedVertexId(insertedVertex?.id)
                                captureStatus = "Vértice insertado en línea."

                                val polylineId = selectedPolylineId!!
                                coroutineScope.launch {
                                    mapViewModel.setSavedPolylines(
                                        persistPolylineVertices(
                                            db = db,
                                            farmId = farmId,
                                            polylineId = polylineId,
                                            vertices = updatedVertices.sortedBy { it.vertexOrder }
                                        )
                                    )
                                }

                                return@setOnTouchListener true
                            }
                        }

                        false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val moveLatLng = map.projection.fromScreenLocation(PointF(event.x, event.y))
                        if (moveLatLng == null) {
                            if (isDraggingPoint || isDraggingVertex) {
                                return@setOnTouchListener true
                            }
                            return@setOnTouchListener false
                        }

                        if (isDraggingPoint && isEditingPoint && selectedPointId != null) {
                            mapViewModel.updatePointPositionInUi(
                                pointId = selectedPointId,
                                latitude = moveLatLng.latitude,
                                longitude = moveLatLng.longitude
                            )

                            view.parent?.requestDisallowInterceptTouchEvent(true)
                            return@setOnTouchListener true
                        }

                        if (isDraggingVertex && selectedVertexId != null) {
                            val vertexId = selectedVertexId!!

                            if (isEditingPolygon) {
                                val updatedVertices = polygonVerticesForEdit.map {
                                    if (it.id == vertexId) {
                                        it.copy(
                                            latitude = moveLatLng.latitude,
                                            longitude = moveLatLng.longitude
                                        )
                                    } else {
                                        it
                                    }
                                }

                                mapViewModel.setSavedPolygons(savedPolygons.map { (poly, verts) ->
                                    if (poly.id == selectedPolygonId) {
                                        poly to updatedVertices
                                    } else {
                                        poly to verts
                                    }
                                })

                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                return@setOnTouchListener true
                            }

                            if (isEditingPolyline) {
                                val updatedVertices = polylineVerticesForEdit.map {
                                    if (it.id == vertexId) {
                                        it.copy(
                                            latitude = moveLatLng.latitude,
                                            longitude = moveLatLng.longitude
                                        )
                                    } else {
                                        it
                                    }
                                }

                                mapViewModel.setSavedPolylines(savedPolylines.map { (line, verts) ->
                                    if (line.id == selectedPolylineId) {
                                        line to updatedVertices
                                    } else {
                                        line to verts
                                    }
                                })

                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                return@setOnTouchListener true
                            }
                        }

                        false
                    }

                    MotionEvent.ACTION_UP -> {
                        val upLatLng = map.projection.fromScreenLocation(PointF(event.x, event.y))

                        if (isDraggingPoint && selectedPointId != null) {
                            view.parent?.requestDisallowInterceptTouchEvent(false)

                            if (upLatLng == null) {
                                mapViewModel.stopPointDrag()
                                return@setOnTouchListener true
                            }

                            coroutineScope.launch {
                                if (db != null) {
                                    mapViewModel.setCapturedPoints(
                                        updatePointPosition(
                                            db = db,
                                            farmId = farmId,
                                            pointId = selectedPointId,
                                            latitude = upLatLng.latitude,
                                            longitude = upLatLng.longitude
                                        )
                                    )
                                }
                                mapViewModel.stopPointDrag()
                                captureStatus = "Punto actualizado."
                            }

                            return@setOnTouchListener true
                        }

                        if (isDraggingVertex && selectedVertexId != null) {
                            view.parent?.requestDisallowInterceptTouchEvent(false)

                            if (upLatLng == null) {
                                mapViewModel.setDraggingVertex(false)
                                return@setOnTouchListener true
                            }

                            coroutineScope.launch {
                                if (isPolygonEditMode) {
                                    val polygonId = selectedPolygonId
                                    if (polygonId != null && db != null) {
                                        val currentVerticesInMemory = savedPolygons
                                            .find { it.first.id == polygonId }
                                            ?.second
                                            ?.sortedBy { it.vertexOrder }
                                            ?: emptyList()

                                        mapViewModel.setSavedPolygons(
                                            persistPolygonVertices(
                                                db = db,
                                                farmId = farmId,
                                                vertices = currentVerticesInMemory
                                            )
                                        )
                                    }
                                } else if (isPolylineEditMode) {
                                    val polylineId = selectedPolylineId
                                    if (polylineId != null && db != null) {
                                        val currentVerticesInMemory = savedPolylines
                                            .find { it.first.id == polylineId }
                                            ?.second
                                            ?.sortedBy { it.vertexOrder }
                                            ?: emptyList()

                                        mapViewModel.setSavedPolylines(
                                            persistPolylineVertices(
                                                db = db,
                                                farmId = farmId,
                                                polylineId = polylineId,
                                                vertices = currentVerticesInMemory
                                            )
                                        )
                                    }
                                }
                                mapViewModel.setDraggingVertex(false)
                                captureStatus = "Vértice actualizado."
                            }

                            return@setOnTouchListener true
                        }

                        false
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        view.parent?.requestDisallowInterceptTouchEvent(false)

                        if (isDraggingPoint && selectedPointId != null) {
                            mapViewModel.stopPointDrag()
                            mapViewModel.clearUndoAction()

                            coroutineScope.launch {
                                if (db != null) {
                                    mapViewModel.reloadCompleteMapData()
                                }
                            }

                            return@setOnTouchListener true
                        }

                        if (isDraggingVertex && selectedVertexId != null) {
                            mapViewModel.setDraggingVertex(false)
                            mapViewModel.setSelectedVertexId(null)
                            mapViewModel.clearUndoAction()

                            coroutineScope.launch {
                                if (db != null) {
                                    mapViewModel.reloadCompleteMapData()
                                }
                            }

                            return@setOnTouchListener true
                        }

                        mapViewModel.setDraggingVertex(false)
                        mapViewModel.stopPointDrag()
                        true
                    }
                    else -> false
                }
            }

            getMapAsync { map ->
                mapLibreMap = map

                map.setStyle(
                    Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
                ) { style ->
                    addOrUpdateFarmMarker(
                        style = style,
                        latitude = latitude,
                        longitude = longitude
                    )

                    addOrUpdateCapturedPoints(
                        style = style,
                        points = capturedPoints,
                        selectedPointId = selectedPointId
                    )

                    addOrUpdateSavedPolygons(
                        style = style,
                        polygons = savedPolygons,
                        selectedPolygonId = selectedPolygonId
                    )

                    addOrUpdateTemporaryPolygonVertices(
                        style = style,
                        vertices = polygonVertices
                    )

                    addOrUpdateTemporaryPolygonLine(
                        style = style,
                        vertices = polygonVertices
                    )

                    addOrUpdateSavedPolylines(
                        style = style,
                        polylines = savedPolylines,
                        selectedPolylineId = selectedPolylineId
                    )

                    addOrUpdateTemporaryPolyline(
                        style = style,
                        vertices = polylineVertices
                    )

                    centerOnFarm(
                        mapLibreMap = mapLibreMap,
                        latitude = latitude,
                        longitude = longitude
                    )
                }

                map.addOnMapClickListener { latLng ->
                    val currentDb = db
                    val currentFarmId = farmId
                    val clickUiState = mapViewModel.uiState
                    val capturedPoints = clickUiState.capturedPoints
                    val savedPolygons = clickUiState.savedPolygons
                    val savedPolylines = clickUiState.savedPolylines
                    val selectedPolygonId = clickUiState.selectedPolygonId
                    val selectedPolylineId = clickUiState.selectedPolylineId
                    val selectedVertexId = clickUiState.selectedVertexId
                    val isPolygonEditMode = clickUiState.isPolygonEditMode
                    val isPolylineEditMode = clickUiState.isPolylineEditMode

                    if (currentDb == null || currentFarmId.isBlank()) {
                        captureStatus = "No se puede guardar el punto en esta vista."
                        return@addOnMapClickListener true
                    }

                    coroutineScope.launch {

                        // 🔴 PRIORIDAD 1: POLÍGONO
                        if (isPolygonCaptureModeEnabled) {
                            val updatedVertices = polygonVertices + latLng
                            polygonVertices = updatedVertices

                            captureStatus = "Vértice agregado al polígono. Total actual: ${updatedVertices.size}"
                            return@launch
                        }

                        // 🟠 PRIORIDAD 1.5: POLILÍNEA
                        if (isPolylineCaptureModeEnabled) {
                            val updatedVertices = polylineVertices + latLng
                            polylineVertices = updatedVertices

                            captureStatus = "Vértice agregado a la línea. Total actual: ${updatedVertices.size}"
                            return@launch
                        }

                        // ðŸŸ¢ PRIORIDAD 2: CAPTURA DE PUNTO
                        if (isCaptureModeEnabled) {
                            val result = createPointAtLocation(
                                db = currentDb,
                                farmId = currentFarmId,
                                captureCategory = captureCategory,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            )

                            val createdPointId = result.first
                            val createdPointName = result.second
                            val savedPoints = result.third

                            mapViewModel.setCapturedPoints(savedPoints)
                            mapViewModel.setSelectedPointId(createdPointId)

                            // 🔥 IMPORTANTE: seguimos desactivando captura (tu lógica original)
                            isCaptureModeEnabled = false

                            val createdPoint = savedPoints.find { it.id == createdPointId }
                            if (createdPoint != null) {
                                centerOnSelectedPoint(
                                    mapLibreMap = mapLibreMap,
                                    point = createdPoint
                                )
                            }

                            captureStatus = "Punto guardado y seleccionado: $createdPointName. Modo captura desactivado."

                            return@launch
                        }

                        // 🟡 PRIORIDAD 2.5: EDICIÓN DE POLÍGONO
                        if (isPolygonEditMode && selectedPolygonId != null) {
                            val polygonVerticesForEdit = savedPolygons
                                .find { it.first.id == selectedPolygonId }
                                ?.second
                                ?.sortedBy { it.vertexOrder }
                                ?: emptyList()

                            val editThreshold = getSelectionThreshold(mapLibreMap) * 1.8

                            val tappedVertex = findNearestVertexToTap(
                                vertices = polygonVerticesForEdit,
                                tapLat = latLng.latitude,
                                tapLng = latLng.longitude,
                                threshold = editThreshold
                            )

                            val tappedSegment = findNearestSegmentToTap(
                                vertices = polygonVerticesForEdit,
                                tapLat = latLng.latitude,
                                tapLng = latLng.longitude,
                                threshold = editThreshold
                            )

                            val isTapInsidePolygon = polygonVerticesForEdit.size >= 3 &&
                                    com.fincagis.app.presentation.main.map.isPointInsidePolygon(
                                        tapLat = latLng.latitude,
                                        tapLng = latLng.longitude,
                                        vertices = polygonVerticesForEdit
                                    )

                            if (tappedVertex != null || tappedSegment != null || isTapInsidePolygon) {
                                captureStatus = if (selectedVertexId != null) {
                                    "Arrastra el vértice seleccionado para moverlo."
                                } else {
                                    "Toca y arrastra un vértice para editarlo."
                                }
                                return@launch
                            }

                            mapViewModel.finishPolygonEdit()
                            captureStatus = "Edición de polígono finalizada."
                        }

                        // ⚪ PRIORIDAD 3: SELECCIÓN

                        val selectionResult = resolveMapSelection(
                            capturedPoints = capturedPoints,
                            savedPolygons = savedPolygons,
                            savedPolylines = savedPolylines,
                            tapLatitude = latLng.latitude,
                            tapLongitude = latLng.longitude,
                            mapLibreMap = mapLibreMap
                        )

                        captureStatus = when (selectionResult) {
                            is MapSelectionResult.PointSelected -> selectionResult.status
                            is MapSelectionResult.PolygonSelected -> selectionResult.status
                            is MapSelectionResult.PolylineSelected -> selectionResult.status
                            is MapSelectionResult.NothingSelected -> selectionResult.status
                        }

                        when (val centerTarget = applySelectionResultState(mapViewModel, selectionResult)) {
                            is MapSelectionCenterTarget.Point -> {
                                centerOnSelectedPoint(
                                    mapLibreMap = mapLibreMap,
                                    point = centerTarget.point
                                )
                            }

                            is MapSelectionCenterTarget.Polygon -> {
                                if (centerTarget.vertices.isNotEmpty()) {
                                    centerOnSelectedPolygon(
                                        mapLibreMap = mapLibreMap,
                                        vertices = centerTarget.vertices
                                    )
                                }
                            }

                            is MapSelectionCenterTarget.Polyline -> {
                                if (centerTarget.vertices.isNotEmpty()) {
                                    centerOnSelectedPolyline(
                                        mapLibreMap = mapLibreMap,
                                        vertices = centerTarget.vertices
                                    )
                                }
                            }

                            MapSelectionCenterTarget.None -> Unit
                        }
                    }

                    true
                }
            }
        }
    }

    LaunchedEffect(latitude, longitude, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateFarmMarker(
                style = style,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    LaunchedEffect(capturedPoints, selectedPointId, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateCapturedPoints(
                style = style,
                points = capturedPoints,
                selectedPointId = selectedPointId
            )
        }
    }

    LaunchedEffect(selectedPolygonId, selectedVertexId, savedPolygons, mapLibreMap) {
        mapLibreMap?.getStyle { style ->

            val vertices = if (selectedPolygonId != null) {
                savedPolygons
                    .find { it.first.id == selectedPolygonId }
                    ?.second ?: emptyList()
            } else {
                emptyList()
            }

            addOrUpdatePolygonVerticesLayer(
                style = style,
                vertices = vertices,
                selectedVertexId = selectedVertexId
            )
        }
    }

    LaunchedEffect(savedPolygons, selectedPolygonId, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateSavedPolygons(
                style = style,
                polygons = savedPolygons,
                selectedPolygonId = selectedPolygonId
            )
        }
    }

    LaunchedEffect(savedPolylines, selectedPolylineId, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateSavedPolylines(
                style = style,
                polylines = savedPolylines,
                selectedPolylineId = selectedPolylineId
            )
        }
    }

    LaunchedEffect(selectedPolylineId, selectedVertexId, savedPolylines, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            val vertices = if (selectedPolylineId != null) {
                savedPolylines
                    .find { it.first.id == selectedPolylineId }
                    ?.second ?: emptyList()
            } else {
                emptyList()
            }

            addOrUpdatePolylineVerticesLayer(
                style = style,
                vertices = vertices,
                selectedVertexId = selectedVertexId
            )
        }
    }

    LaunchedEffect(polygonVertices, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateTemporaryPolygonVertices(
                style = style,
                vertices = polygonVertices
            )

            addOrUpdateTemporaryPolygonLine(
                style = style,
                vertices = polygonVertices
            )
        }
    }

    LaunchedEffect(polylineVertices, mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            addOrUpdateTemporaryPolyline(
                style = style,
                vertices = polylineVertices
            )
        }
    }

    LaunchedEffect(userLatitude, userLongitude, mapLibreMap, latitude, longitude) {
        val userLat = userLatitude
        val userLon = userLongitude
        val map = mapLibreMap

        if (userLat != null && userLon != null && map != null) {
            map.getStyle { style ->
                addOrUpdateUserMarker(
                    style = style,
                    latitude = userLat,
                    longitude = userLon
                )

                addOrUpdateFarmMarker(
                    style = style,
                    latitude = latitude,
                    longitude = longitude
                )
            }

            centerOnBoth(
                mapLibreMap = mapLibreMap,
                farmLatitude = latitude,
                farmLongitude = longitude,
                userLatitude = userLatitude,
                userLongitude = userLongitude
            )
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Mapa",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Volver",
                onNavigationClick = onBackClick
            )
        }
    ) { innerPadding ->
        val screenScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(
                    state = screenScrollState,
                    enabled = !isDraggingVertex && !isDraggingPoint
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mapa de $farmName",
                style = MaterialTheme.typography.headlineSmall
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Visor de mapa",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.1f),
                        factory = { mapView }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { centerOnUser(
                                mapLibreMap = mapLibreMap,
                                userLatitude = userLatitude,
                                userLongitude = userLongitude
                            ) },
                            enabled = userLatitude != null && userLongitude != null
                        ) {
                            Text("Mi ubicación")
                        }

                        Button(
                            onClick = {
                                centerOnFarm(
                                    mapLibreMap = mapLibreMap,
                                    latitude = latitude,
                                    longitude = longitude
                                )
                            }
                        ) {
                            Text("Finca")
                        }

                        Button(
                            onClick = {
                                centerOnBoth(
                                    mapLibreMap = mapLibreMap,
                                    farmLatitude = latitude,
                                    farmLongitude = longitude,
                                    userLatitude = userLatitude,
                                    userLongitude = userLongitude
                                )
                            }
                        ) {
                            Text("Ambos")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isCaptureModeEnabled = true
                                isPolygonCaptureModeEnabled = false
                                mapViewModel.setSelectedPointId(null)
                                mapViewModel.setSelectedPolygonId(null)
                                isPolylineCaptureModeEnabled = false
                                mapViewModel.setSelectedPolylineId(null)
                                captureStatus = "Nueva captura iniciada. Categoría actual: $captureCategory. Toca el mapa para crear un punto."
                            },
                            colors = if (isCaptureModeEnabled && !isPolygonCaptureModeEnabled) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Nuevo punto")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val suggestedName = withContext(Dispatchers.IO) {
                                        val existingPolygons = db?.polygonDao()?.getPolygonsByFarmId(farmId) ?: emptyList()
                                        buildNextPolygonName(existingPolygons)
                                    }

                                    isCaptureModeEnabled = false
                                    isPolygonCaptureModeEnabled = true
                                    isPolylineCaptureModeEnabled = false
                                    mapViewModel.setSelectedPointId(null)
                                    mapViewModel.setSelectedPolygonId(null)
                                    mapViewModel.setSelectedPolylineId(null)
                                    polygonVertices = emptyList()
                                    polygonNameInput = suggestedName
                                    polygonDescriptionInput = ""
                                    captureStatus = "Modo levantamiento de polígono activado. Toca el mapa para agregar vértices."
                                }
                            },
                            colors = if (isPolygonCaptureModeEnabled) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Nuevo polígono")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val suggestedName = withContext(Dispatchers.IO) {
                                        val existingPolylines = db?.polylineDao()?.getPolylinesByFarmId(farmId) ?: emptyList()
                                        buildNextPolylineName(existingPolylines)
                                    }

                                    isCaptureModeEnabled = false
                                    isPolygonCaptureModeEnabled = false
                                    isPolylineCaptureModeEnabled = true
                                    mapViewModel.setSelectedPointId(null)
                                    mapViewModel.setSelectedPolygonId(null)
                                    mapViewModel.setSelectedPolylineId(null)
                                    polylineVertices = emptyList()
                                    polylineNameInput = suggestedName
                                    polylineDescriptionInput = ""
                                    captureStatus = "Modo levantamiento de línea activado. Toca el mapa para agregar vértices."
                                }
                            },
                            colors = if (isPolylineCaptureModeEnabled) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Nueva línea")
                        }

                        Button(
                            onClick = {
                                isCaptureModeEnabled = false
                                isPolygonCaptureModeEnabled = false
                                isPolylineCaptureModeEnabled = false
                                mapViewModel.setSelectedPolygonId(null)
                                mapViewModel.setSelectedPolylineId(null)
                                mapViewModel.setPolygonEditMode(false)
                                mapViewModel.setPolylineEditMode(false)
                                mapViewModel.setSelectedVertexId(null)
                                mapViewModel.setDraggingVertex(false)
                                polygonVertices = emptyList()
                                polygonNameInput = ""
                                polygonDescriptionInput = ""
                                polylineVertices = emptyList()
                                polylineNameInput = ""
                                polylineDescriptionInput = ""
                                captureStatus = "Modo captura desactivado. Ahora solo seleccionas puntos."
                            },
                            colors = if (!isCaptureModeEnabled && !isPolygonCaptureModeEnabled) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Desactivar captura")
                        }
                    }

                    Text(
                        text = "Categoría activa para nuevas capturas",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pointCategories.forEach { category ->
                            val categoryColor = colorFromHex(getPointColorByCategory(category))
                            val isActiveCaptureCategory = captureCategory == category

                            Button(
                                onClick = {
                                    captureCategory = category
                                    captureStatus = "Categoría activa de captura: $category"
                                },
                                colors = if (isActiveCaptureCategory) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = categoryColor
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(category)
                            }
                        }
                    }

                    Text(
                        text = "Modo actual: ${
                            when {
                                isPolygonCaptureModeEnabled -> "Levantamiento de polígono"
                                isPolylineCaptureModeEnabled -> "Levantamiento de línea"
                                isCaptureModeEnabled -> "Captura de puntos"
                                else -> "Selección"
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "Categoría de captura: $captureCategory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorFromHex(getPointColorByCategory(captureCategory)),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Información de ubicación",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                text = "Latitud finca: ${formatCoordinate(latitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text = "Longitud finca: ${formatCoordinate(longitude)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Estado ubicación: $locationStatus",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text = "Latitud usuario: ${userLatitude?.let { formatCoordinate(it) } ?: "Sin dato"}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Longitud usuario: ${userLongitude?.let { formatCoordinate(it) } ?: "Sin dato"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Text(
                        text = "Estado captura: $captureStatus",

                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    if (hasUndoAction) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    mapViewModel.undoLastMovement()
                                    captureStatus = "Último movimiento deshecho."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Deshacer último movimiento")
                        }
                    }

                    if (isPolygonCaptureModeEnabled) {
                        Text(
                            text = "Vértices del polígono actual: ${polygonVertices.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Datos del polígono en levantamiento",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                OutlinedTextField(
                                    value = polygonNameInput,
                                    onValueChange = { polygonNameInput = it },
                                    label = { Text("Nombre del polígono") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = polygonDescriptionInput,
                                    onValueChange = { polygonDescriptionInput = it },
                                    label = { Text("Descripción del polígono") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    minLines = 3
                                )

                                Button(
                                    onClick = {
                                        if (polygonVertices.size < 3) {
                                            captureStatus = "Un polígono necesita al menos 3 vértices."
                                            return@Button
                                        }

                                        val trimmedName = polygonNameInput.trim()
                                        if (trimmedName.isBlank()) {
                                            captureStatus = "El nombre del polígono no puede estar vacío."
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            val safeDb = db ?: return@launch

                                            val updatedPolygons = savePolygon(
                                                db = safeDb,
                                                farmId = farmId,
                                                polygonName = trimmedName,
                                                polygonDescription = polygonDescriptionInput.trim(),
                                                polygonVertices = polygonVertices
                                            )

                                            mapViewModel.setSavedPolygons(updatedPolygons)
                                            isPolygonCaptureModeEnabled = false
                                            polygonVertices = emptyList()
                                            polygonNameInput = ""
                                            polygonDescriptionInput = ""
                                            captureStatus = "Polígono guardado correctamente."
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    enabled = polygonVertices.size >= 3
                                ) {
                                    Text("Guardar polígono")
                                }

                                Button(
                                    onClick = {
                                        polygonVertices = emptyList()
                                        polygonDescriptionInput = ""
                                        captureStatus = "Levantamiento de polígono reiniciado."
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Limpiar vértices")
                                }

                                Button(
                                    onClick = {
                                        isPolygonCaptureModeEnabled = false
                                        polygonVertices = emptyList()
                                        polygonNameInput = ""
                                        polygonDescriptionInput = ""
                                        captureStatus = "Levantamiento de polígono cancelado."
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Cancelar polígono")
                                }
                            }
                        }
                    }

                    if (isPolylineCaptureModeEnabled) {
                        Text(
                            text = "Vértices de la línea actual: ${polylineVertices.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Datos de la línea en levantamiento",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                OutlinedTextField(
                                    value = polylineNameInput,
                                    onValueChange = { polylineNameInput = it },
                                    label = { Text("Nombre de la línea") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = polylineDescriptionInput,
                                    onValueChange = { polylineDescriptionInput = it },
                                    label = { Text("Descripción de la línea") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    minLines = 3
                                )

                                Button(
                                    onClick = {
                                        if (polylineVertices.size < 2) {
                                            captureStatus = "Una línea necesita al menos 2 vértices."
                                            return@Button
                                        }

                                        val trimmedName = polylineNameInput.trim()
                                        if (trimmedName.isBlank()) {
                                            captureStatus = "El nombre de la línea no puede estar vacío."
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            val safeDb = db ?: return@launch

                                            val updatedPolylines = savePolyline(
                                                db = safeDb,
                                                farmId = farmId,
                                                polylineName = trimmedName,
                                                polylineDescription = polylineDescriptionInput.trim(),
                                                polylineVertices = polylineVertices
                                            )

                                            mapViewModel.setSavedPolylines(updatedPolylines)
                                            isPolylineCaptureModeEnabled = false
                                            polylineVertices = emptyList()
                                            polylineNameInput = ""
                                            polylineDescriptionInput = ""
                                            captureStatus = "Línea guardada correctamente."
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    enabled = polylineVertices.size >= 2
                                ) {
                                    Text("Guardar línea")
                                }

                                Button(
                                    onClick = {
                                        polylineVertices = emptyList()
                                        polylineDescriptionInput = ""
                                        captureStatus = "Levantamiento de línea reiniciado."
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Limpiar vértices")
                                }

                                Button(
                                    onClick = {
                                        isPolylineCaptureModeEnabled = false
                                        polylineVertices = emptyList()
                                        polylineNameInput = ""
                                        polylineDescriptionInput = ""
                                        captureStatus = "Levantamiento de línea cancelado."
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Cancelar línea")
                                }
                            }
                        }
                    }

                    Text(
                        text = "Polígonos guardados: ${savedPolygons.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    if (savedPolygons.isEmpty()) {
                        Text(
                            text = "Todavía no hay polígonos guardados para esta finca.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        savedPolygons.forEachIndexed { index, (polygon, vertices) ->
                            val isSelected = polygon.id == selectedPolygonId
                            val borderWidth: Dp = if (isSelected) 2.dp else 1.dp

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable {
                                        val isNowSelected = handlePolygonListSelection(
                                            mapViewModel = mapViewModel,
                                            isSelected = isSelected,
                                            polygonId = polygon.id
                                        )

                                        if (!isNowSelected) {
                                            captureStatus = "Polígono deseleccionado desde la lista."
                                        } else {
                                            captureStatus = "Polígono seleccionado: ${polygon.name}"
                                            centerOnSelectedPolygon(
                                                mapLibreMap = mapLibreMap,
                                                vertices = vertices
                                            )
                                        }
                                    }
                                    .border(
                                        width = borderWidth,
                                        color = colorFromHex("#8E24AA"),
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${polygon.name}",
                                        color = colorFromHex("#8E24AA"),
                                        style = if (isSelected) {
                                            MaterialTheme.typography.titleMedium
                                        } else {
                                            MaterialTheme.typography.bodyLarge
                                        }
                                    )

                                    Text(
                                        text = "Vértices: ${vertices.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Descripción: ${formatPointDescription(polygon.description)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Creado: ${formatTimestamp(polygon.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPolygon != null) {
                        val polygon = selectedPolygon.first
                        val vertices = selectedPolygon.second

                        Text(
                            text = "Polígono seleccionado",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorFromHex("#8E24AA"),
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Nombre: ${polygon.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Descripción: ${formatPointDescription(polygon.description)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    text = "Cantidad de vértices: ${vertices.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    text = "Creado: ${formatTimestamp(polygon.createdAt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = selectedPolygonNameInput,
                            onValueChange = { selectedPolygonNameInput = it },
                            label = { Text("Nombre del polígono") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = selectedPolygonDescriptionInput,
                            onValueChange = { selectedPolygonDescriptionInput = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = selectedPolygonCategoryInput,
                            onValueChange = { selectedPolygonCategoryInput = it },
                            label = { Text("Categoría") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val trimmedName = selectedPolygonNameInput.trim()
                                val trimmedDescription = selectedPolygonDescriptionInput.trim()
                                val trimmedCategory = selectedPolygonCategoryInput.trim()

                                if (trimmedName.isBlank()) {
                                    captureStatus = "El nombre del polígono no puede estar vacío."
                                    return@Button
                                }

                                if (trimmedCategory.isBlank()) {
                                    captureStatus = "La categoría del polígono no puede estar vacía."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    val updated = mapViewModel.updateSelectedPolygonAttributes(
                                        name = trimmedName,
                                        description = trimmedDescription,
                                        category = trimmedCategory
                                    )

                                    captureStatus = if (updated) {
                                        "Atributos del polígono actualizados."
                                    } else {
                                        "No se pudieron guardar los atributos del polígono."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Guardar atributos")
                        }

                        Button(
                            onClick = {
                                val isEditEnabled = togglePolygonEditMode(mapViewModel)

                                captureStatus = if (isEditEnabled) {
                                    "Modo edición activado. Toca y arrastra un vértice, o selecciónalo para eliminarlo."
                                } else {
                                    "Modo edición desactivado."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                if (isPolygonEditMode) "Desactivar edición de polígono"
                                else "Editar polígono"
                            )
                        }

                        Button(
                            onClick = {
                                centerOnSelectedPolygon(
                                    mapLibreMap = mapLibreMap,
                                    vertices = vertices
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Centrar polígono en mapa")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val polygonId = selectedPolygonId
                                    val vertexId = selectedVertexId
                                    val safeDb = db

                                    if (polygonId == null || vertexId == null || safeDb == null) {
                                        return@launch
                                    }

                                    val currentVertices = getVerticesOfSelectedPolygon(
                                        savedPolygons = savedPolygons,
                                        selectedPolygonId = selectedPolygonId
                                    ).sortedBy { it.vertexOrder }

                                    if (currentVertices.size <= 3) {
                                        captureStatus = "Un polígono debe conservar al menos 3 vértices."
                                        return@launch
                                    }

                                    mapViewModel.setSavedPolygons(
                                        deleteSelectedVertexFromPolygon(
                                            db = safeDb,
                                            farmId = farmId,
                                            polygonId = polygonId,
                                            vertexId = vertexId,
                                            currentVertices = currentVertices
                                        )
                                    )

                                    mapViewModel.setSelectedVertexId(null)
                                    captureStatus = "Vértice eliminado correctamente."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = isPolygonEditMode && selectedVertexId != null
                        ) {
                            Text("Eliminar vértice seleccionado")
                        }

                        Button(
                            onClick = {
                                val polygonId = polygon.id

                                coroutineScope.launch {
                                    val safeDb = db ?: return@launch

                                    mapViewModel.setSavedPolygons(
                                        deletePolygonById(
                                            db = safeDb,
                                            farmId = farmId,
                                            polygonId = polygonId
                                        )
                                    )

                                    clearAfterPolygonDelete(mapViewModel)
                                    captureStatus = "Polígono eliminado correctamente."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Eliminar polígono seleccionado")
                        }

                        Button(
                            onClick = {
                                clearAfterPolygonDeselect(mapViewModel)
                                captureStatus = "Polígono deseleccionado."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Deseleccionar polígono")
                        }
                    }

                    Text(
                        text = "Líneas guardadas: ${savedPolylines.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    if (savedPolylines.isEmpty()) {
                        Text(
                            text = "Todavía no hay líneas guardadas para esta finca.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        savedPolylines.forEachIndexed { index, (polyline, vertices) ->
                            val isSelected = polyline.id == selectedPolylineId
                            val borderWidth: Dp = if (isSelected) 2.dp else 1.dp

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable {
                                        val isNowSelected = handlePolylineListSelection(
                                            mapViewModel = mapViewModel,
                                            isSelected = isSelected,
                                            polylineId = polyline.id
                                        )

                                        if (!isNowSelected) {
                                            captureStatus = "Línea deseleccionada desde la lista."
                                        } else {
                                            captureStatus = "Línea seleccionada: ${polyline.name}"

                                            centerOnSelectedPolyline(
                                                mapLibreMap = mapLibreMap,
                                                vertices = vertices
                                            )
                                        }
                                    }
                                    .border(
                                        width = borderWidth,
                                        color = colorFromHex("#00897B"),
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${polyline.name}",
                                        color = colorFromHex("#00897B"),
                                        style = if (isSelected) {
                                            MaterialTheme.typography.titleMedium
                                        } else {
                                            MaterialTheme.typography.bodyLarge
                                        }
                                    )

                                    Text(
                                        text = "Vértices: ${vertices.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Descripción: ${formatPointDescription(polyline.description)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Creado: ${formatTimestamp(polyline.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPolyline != null) {
                        val polyline = selectedPolyline.first
                        val vertices = selectedPolyline.second

                        Text(
                            text = "Línea seleccionada",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorFromHex("#00897B"),
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Nombre: ${polyline.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Descripción: ${formatPointDescription(polyline.description)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    text = "Cantidad de vértices: ${vertices.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    text = "Creado: ${formatTimestamp(polyline.createdAt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = selectedPolylineNameInput,
                            onValueChange = { selectedPolylineNameInput = it },
                            label = { Text("Nombre de la línea") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = selectedPolylineDescriptionInput,
                            onValueChange = { selectedPolylineDescriptionInput = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = selectedPolylineCategoryInput,
                            onValueChange = { selectedPolylineCategoryInput = it },
                            label = { Text("Categoría") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val trimmedName = selectedPolylineNameInput.trim()
                                val trimmedDescription = selectedPolylineDescriptionInput.trim()
                                val trimmedCategory = selectedPolylineCategoryInput.trim()

                                if (trimmedName.isBlank()) {
                                    captureStatus = "El nombre de la línea no puede estar vacío."
                                    return@Button
                                }

                                if (trimmedCategory.isBlank()) {
                                    captureStatus = "La categoría de la línea no puede estar vacía."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    val updated = mapViewModel.updateSelectedPolylineAttributes(
                                        name = trimmedName,
                                        description = trimmedDescription,
                                        category = trimmedCategory
                                    )

                                    captureStatus = if (updated) {
                                        "Atributos de la línea actualizados."
                                    } else {
                                        "No se pudieron guardar los atributos de la línea."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Guardar atributos")
                        }

                        Button(
                            onClick = {
                                val isEditEnabled = togglePolylineEditMode(mapViewModel)

                                captureStatus = if (isEditEnabled) {
                                    "Modo edición de línea activado. Toca y arrastra un vértice."
                                } else {
                                    "Modo edición de línea desactivado."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                if (isPolylineEditMode) "Desactivar edición de línea"
                                else "Editar línea"
                            )
                        }

                        Button(
                            onClick = {
                                centerOnSelectedPolyline(
                                    mapLibreMap = mapLibreMap,
                                    vertices = vertices
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Centrar línea en mapa")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val polylineId = selectedPolylineId
                                    val vertexId = selectedVertexId
                                    val safeDb = db

                                    if (polylineId == null || vertexId == null || safeDb == null) {
                                        return@launch
                                    }

                                    val currentVertices = getVerticesOfSelectedPolyline(
                                        savedPolylines = savedPolylines,
                                        selectedPolylineId = selectedPolylineId
                                    ).sortedBy { it.vertexOrder }

                                    if (currentVertices.size <= 2) {
                                        captureStatus = "Una línea debe conservar al menos 2 vértices."
                                        return@launch
                                    }

                                    mapViewModel.setSavedPolylines(
                                        deleteSelectedVertexFromPolyline(
                                            db = safeDb,
                                            farmId = farmId,
                                            polylineId = polylineId,
                                            vertexId = vertexId,
                                            currentVertices = currentVertices
                                        )
                                    )

                                    mapViewModel.setSelectedVertexId(null)
                                    captureStatus = "Vértice de línea eliminado correctamente."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = isPolylineEditMode && selectedVertexId != null
                        ) {
                            Text("Eliminar vértice de línea")
                        }

                        Button(
                            onClick = {
                                mapViewModel.setSelectedPolylineId(null)
                                captureStatus = "Línea deseleccionada."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Deseleccionar línea")
                        }

                        Button(
                            onClick = {
                                val polylineId = polyline.id

                                coroutineScope.launch {
                                    val safeDb = db ?: return@launch

                                    mapViewModel.setSavedPolylines(
                                        deletePolylineById(
                                            db = safeDb,
                                            farmId = farmId,
                                            polylineId = polylineId
                                        )
                                    )

                                    mapViewModel.setSelectedPolylineId(null)
                                    captureStatus = "Línea eliminada correctamente."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Eliminar línea seleccionada")
                        }
                    }

                    Text(
                        text = "Puntos capturados: ${capturedPoints.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    if (capturedPoints.isEmpty()) {
                        Text(
                            text = "Todavía no hay puntos guardados para esta finca.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        capturedPoints.forEachIndexed { index, point ->
                            val isSelected = point.id == selectedPointId
                            val categoryColor = colorFromHex(getPointColorByCategory(point.category))
                            val borderWidth: Dp = if (isSelected) 2.dp else 1.dp

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable {
                                        val isNowSelected = handlePointListSelection(
                                            mapViewModel = mapViewModel,
                                            isSelected = isSelected,
                                            pointId = point.id
                                        )

                                        if (!isNowSelected) {
                                            captureStatus = "Punto deseleccionado desde la lista."
                                        } else {
                                            captureStatus = "Punto seleccionado desde la lista: ${point.name}"
                                            centerOnSelectedPoint(
                                                mapLibreMap = mapLibreMap,
                                                point = point
                                            )
                                        }
                                    }
                                    .border(
                                        width = borderWidth,
                                        color = categoryColor,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${point.name}",
                                        color = categoryColor,
                                        style = if (isSelected) {
                                            MaterialTheme.typography.titleMedium
                                        } else {
                                            MaterialTheme.typography.bodyLarge
                                        }
                                    )

                                    Text(
                                        text = "Categoría: ${point.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = categoryColor
                                    )

                                    Text(
                                        text = "Lat: ${formatCoordinate(point.latitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Lon: ${formatCoordinate(point.longitude)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "Descripción: ${formatPointDescription(point.description)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Text(
                                        text = "Creado: ${formatTimestamp(point.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPoint != null) {
                        val activeCategoryColor = colorFromHex(getPointColorByCategory(pointCategoryInput))
                        Text(
                            text = "Editar punto seleccionado",
                            style = MaterialTheme.typography.titleMedium,
                            color = activeCategoryColor,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Categoría activa",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .aspectRatio(1f)
                                            .background(
                                                color = activeCategoryColor,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )

                                    Text(
                                        text = pointCategoryInput,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = activeCategoryColor
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Coordenadas del punto seleccionado",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "Latitud: ${formatCoordinate(selectedPoint.latitude)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    text = "Longitud: ${formatCoordinate(selectedPoint.longitude)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                copySelectedPointCoordinates(selectedPoint)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Copiar coordenadas")
                        }

                        Button(
                            onClick = {
                                val isEditEnabled = togglePointEditMode(mapViewModel)
                                captureStatus = if (isEditEnabled) {
                                    "Modo edición de punto activado. Toca y arrastra el punto seleccionado."
                                } else {
                                    "Modo edición de punto desactivado."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                if (isPointEditMode) "Desactivar edición de punto"
                                else "Editar punto"
                            )
                        }

                        OutlinedTextField(
                            value = pointNameInput,
                            onValueChange = { pointNameInput = it },
                            label = { Text("Nombre del punto") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = pointDescriptionInput,
                            onValueChange = { pointDescriptionInput = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            minLines = 3
                        )

                        Text(
                            text = "Categoría del punto",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pointCategories.forEach { category ->
                                val categoryColor = colorFromHex(getPointColorByCategory(category))
                                val isActiveCategory = pointCategoryInput == category

                                Button(
                                    onClick = {
                                        if (pointCategoryInput != category) {
                                            val shouldAutoRename =
                                                pointNameInput.trim().isBlank() ||
                                                        isAutoGeneratedPointName(
                                                            name = pointNameInput,
                                                            categories = pointCategories
                                                        )

                                            pointCategoryInput = category

                                            if (shouldAutoRename) {
                                                pointNameInput = buildAutoNameForEditedPoint(
                                                    category = category,
                                                    existingPoints = capturedPoints,
                                                    currentPointId = selectedPoint.id
                                                )
                                                captureStatus = "Nombre sugerido actualizado según la categoría."
                                            }
                                        }
                                    },
                                    colors = if (isActiveCategory) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = categoryColor
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors()
                                    }
                                ) {
                                    Text(category)
                                }
                            }
                        }
                        Button(
                            onClick = {
                                val trimmedName = pointNameInput.trim()
                                val trimmedDescription = pointDescriptionInput.trim()
                                val trimmedCategory = pointCategoryInput.trim()

                                if (trimmedName.isBlank()) {
                                    captureStatus = "El nombre del punto no puede estar vacío."
                                    return@Button
                                }

                                if (trimmedCategory.isBlank()) {
                                    captureStatus = "La categoría del punto no puede estar vacía."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    val updated = mapViewModel.updateSelectedPointAttributes(
                                        name = trimmedName,
                                        description = trimmedDescription,
                                        category = trimmedCategory
                                    )

                                    captureStatus = if (updated) {
                                        "Atributos del punto actualizados."
                                    } else {
                                        "No se pudieron guardar los atributos del punto."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text("Guardar atributos del punto")
                        }

                        Button(
                            onClick = {
                                mapViewModel.clearPointSelection()
                                captureStatus = "Punto deseleccionado."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Deseleccionar punto")
                        }

                        Button(
                            onClick = {
                                val pointId = selectedPoint.id

                                coroutineScope.launch {
                                    val safeDb = db ?: return@launch

                                    val updatedPoints = deletePointById(
                                        db = safeDb,
                                        farmId = farmId,
                                        pointId = pointId
                                    )

                                    mapViewModel.setCapturedPoints(updatedPoints)
                                    mapViewModel.clearPointSelection()
                                    captureStatus = "Punto eliminado"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Eliminar punto seleccionado")
                        }
                    }
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted || coarseLocationGranted
}

@Preview(showBackground = true)
@Composable
fun MapPlaceholderScreenPreview() {
    FincagisTheme {
        MapPlaceholderScreen(
            farmName = "Finca San José",
            latitude = -4.036,
            longitude = -79.201,
            onBackClick = {}
        )
    }
}

