package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import com.fincagis.app.data.local.entity.PolylineVertexEntity

fun centerOnFarm(
    mapLibreMap: MapLibreMap?,
    latitude: Double,
    longitude: Double
) {
    mapLibreMap?.easeCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(17.0)
                .build()
        )
    )
}

fun centerOnUser(
    mapLibreMap: MapLibreMap?,
    userLatitude: Double?,
    userLongitude: Double?
) {
    val userLat = userLatitude
    val userLon = userLongitude

    if (userLat != null && userLon != null) {
        mapLibreMap?.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(userLat, userLon))
                    .zoom(17.0)
                    .build()
            )
        )
    }
}

fun centerOnBoth(
    mapLibreMap: MapLibreMap?,
    farmLatitude: Double,
    farmLongitude: Double,
    userLatitude: Double?,
    userLongitude: Double?
) {
    val userLat = userLatitude
    val userLon = userLongitude
    val map = mapLibreMap

    if (userLat != null && userLon != null && map != null) {
        val bounds = LatLngBounds.Builder()
            .include(LatLng(farmLatitude, farmLongitude))
            .include(LatLng(userLat, userLon))
            .build()

        map.easeCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, 120)
        )
    } else {
        centerOnFarm(
            mapLibreMap = mapLibreMap,
            latitude = farmLatitude,
            longitude = farmLongitude
        )
    }
}

fun centerOnSelectedPoint(
    mapLibreMap: MapLibreMap?,
    point: MapPointEntity
) {
    mapLibreMap?.easeCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(point.latitude, point.longitude))
                .zoom(18.0)
                .build()
        )
    )
}

fun centerOnSelectedPolygon(
    mapLibreMap: MapLibreMap?,
    vertices: List<PolygonVertexEntity>
) {
    val map = mapLibreMap ?: return
    if (vertices.size < 3) return

    val boundsBuilder = LatLngBounds.Builder()

    vertices
        .sortedBy { it.vertexOrder }
        .forEach { vertex ->
            boundsBuilder.include(
                LatLng(vertex.latitude, vertex.longitude)
            )
        }

    val bounds = boundsBuilder.build()
    map.easeCamera(
        CameraUpdateFactory.newLatLngBounds(bounds, 120)
    )
}

fun centerOnSelectedPolyline(
    mapLibreMap: MapLibreMap?,
    vertices: List<PolylineVertexEntity>
) {
    val map = mapLibreMap ?: return
    if (vertices.size < 2) return

    val boundsBuilder = LatLngBounds.Builder()

    vertices
        .sortedBy { it.vertexOrder }
        .forEach { vertex ->
            boundsBuilder.include(
                LatLng(vertex.latitude, vertex.longitude)
            )
        }

    val bounds = boundsBuilder.build()
    map.easeCamera(
        CameraUpdateFactory.newLatLngBounds(bounds, 120)
    )
}