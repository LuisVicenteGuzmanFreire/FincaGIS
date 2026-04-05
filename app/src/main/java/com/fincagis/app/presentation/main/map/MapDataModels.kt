package com.fincagis.app.presentation.main.map

import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity

data class MapScreenData(
    val points: List<MapPointEntity>,
    val polygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    val polylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>
)

