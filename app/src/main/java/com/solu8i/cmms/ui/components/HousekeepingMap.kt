package com.solu8i.cmms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import com.solu8i.cmms.data.model.HousekeepingFacilityWithTasks

/**
 * Style peta: raster tile OpenStreetMap, sama seperti peta di web.
 * Ganti [OSM_STYLE_JSON] bila memakai penyedia tile lain (mis. MapTiler).
 */
private const val OSM_STYLE_JSON = """
{
  "version": 8,
  "name": "OSM Raster",
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    { "id": "osm", "type": "raster", "source": "osm" }
  ]
}
"""

private val AREA_OUTLINE = Color(0xFF015E4E)
private val AREA_FILL = Color(0x33015E4E)
private val FACILITY_OUTLINE = Color(0xFFD97706)
private val FACILITY_FILL = Color(0x40D97706)
private val FACILITY_POINT = Color(0xFFD97706)

/**
 * Peta read-only batas area housekeeping beserta polygon fasilitas di dalamnya.
 * Koordinat dari API memakai format [lat, lng]; GeoJSON membutuhkan [lng, lat].
 */
@Composable
fun HousekeepingAreaMap(
    areaPolygon: List<List<Double>>?,
    facilities: List<HousekeepingFacilityWithTasks>,
    modifier: Modifier = Modifier,
    height: Int = 260,
) {
    HousekeepingPolygonMap(
        areaPolygon = areaPolygon,
        facilityPolygons = facilities.mapNotNull { it.polygon },
        facilityPoints = facilities.mapNotNull { facility ->
            val lat = facility.latitude
            val lng = facility.longitude
            if (facility.polygon.isNullOrEmpty() && lat != null && lng != null) lng to lat else null
        },
        modifier = modifier,
        height = height,
    )
}

/**
 * Versi generik: batas area + daftar polygon fasilitas + titik fasilitas
 * (dipakai layar Plan yang hanya punya data fasilitas dari checklist).
 */
@Composable
fun HousekeepingPolygonMap(
    areaPolygon: List<List<Double>>?,
    facilityPolygons: List<List<List<Double>>> = emptyList(),
    facilityPoints: List<Pair<Double, Double>> = emptyList(),
    modifier: Modifier = Modifier,
    height: Int = 260,
) {
    val areaRing = remember(areaPolygon) { toRing(areaPolygon) }
    val polygons = remember(facilityPolygons) {
        facilityPolygons.mapNotNull { polygon -> toRing(polygon) }.distinct()
    }
    val points = remember(facilityPoints) { facilityPoints.distinct() }

    if (areaRing == null && polygons.isEmpty() && points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Area ini belum memiliki batas (polygon) pada peta.",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
            )
        }
        return
    }

    val cameraState = rememberCameraState()
    val bounds = remember(areaRing, polygons, points) {
        buildBounds(areaRing, polygons, points)
    }

    LaunchedEffect(bounds) {
        if (bounds != null) {
            cameraState.animateTo(bounds)
        }
    }

    val areaJson = remember(areaRing) { areaRing?.let { singlePolygonCollection(it) } }
    val facilityJson = remember(polygons) {
        polygons.takeIf { it.isNotEmpty() }?.let { polygonCollection(it) }
    }
    val pointsJson = remember(points) {
        points.takeIf { it.isNotEmpty() }?.let { pointCollection(it) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        MaplibreMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(12.dp)),
            baseStyle = BaseStyle.Json(OSM_STYLE_JSON),
            cameraState = cameraState,
            zoomRange = 3f..18f,
        ) {
            if (areaJson != null) {
                val areaSource = rememberGeoJsonSource(GeoJsonData.JsonString(areaJson))
                FillLayer(
                    id = "hk-area",
                    source = areaSource,
                    color = const(AREA_FILL),
                    outlineColor = const(AREA_OUTLINE),
                )
            }

            if (facilityJson != null) {
                val facilitySource = rememberGeoJsonSource(GeoJsonData.JsonString(facilityJson))
                FillLayer(
                    id = "hk-facilities",
                    source = facilitySource,
                    color = const(FACILITY_FILL),
                    outlineColor = const(FACILITY_OUTLINE),
                )
            }

            if (pointsJson != null) {
                val pointSource = rememberGeoJsonSource(GeoJsonData.JsonString(pointsJson))
                CircleLayer(
                    id = "hk-facility-points",
                    source = pointSource,
                    color = const(FACILITY_POINT),
                    radius = const(6.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem(color = AREA_OUTLINE, label = "Batas area")
            LegendItem(color = FACILITY_OUTLINE, label = "Fasilitas")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
    }
}

/** Ubah [[lat, lng], ...] menjadi ring GeoJSON [[lng, lat], ...] yang tertutup. */
private fun toRing(polygon: List<List<Double>>?): List<Pair<Double, Double>>? {
    if (polygon == null || polygon.size < 3) {
        return null
    }

    val ring = polygon.mapNotNull { point ->
        val lat = point.getOrNull(0)
        val lng = point.getOrNull(1)
        if (lat != null && lng != null) lng to lat else null
    }

    if (ring.size < 3) {
        return null
    }

    return if (ring.first() == ring.last()) ring else ring + ring.first()
}

private fun ringJson(ring: List<Pair<Double, Double>>): String =
    ring.joinToString(prefix = "[", postfix = "]") { (lng, lat) -> "[$lng,$lat]" }

private fun polygonCollection(rings: List<List<Pair<Double, Double>>>): String =
    rings.joinToString(prefix = """{"type":"FeatureCollection","features":[""", postfix = "]}") { ring ->
        """{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[${ringJson(ring)}]}}"""
    }

private fun singlePolygonCollection(ring: List<Pair<Double, Double>>): String =
    polygonCollection(listOf(ring))

private fun pointCollection(points: List<Pair<Double, Double>>): String =
    points.joinToString(prefix = """{"type":"FeatureCollection","features":[""", postfix = "]}") { (lng, lat) ->
        """{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[$lng,$lat]}}"""
    }

private fun buildBounds(
    areaRing: List<Pair<Double, Double>>?,
    facilityPolygons: List<List<Pair<Double, Double>>>,
    facilityPoints: List<Pair<Double, Double>>,
): BoundingBox? {
    val all = buildList {
        areaRing?.let { addAll(it) }
        facilityPolygons.forEach { addAll(it) }
        addAll(facilityPoints)
    }

    if (all.isEmpty()) {
        return null
    }

    val lngs = all.map { it.first }
    val lats = all.map { it.second }

    val west = lngs.min()
    val east = lngs.max()
    val south = lats.min()
    val north = lats.max()

    // Titik tunggal / area sangat kecil: beri sedikit ruang agar peta tidak zoom ekstrem.
    val padLng = maxOf((east - west) * 0.1, 0.0005)
    val padLat = maxOf((north - south) * 0.1, 0.0005)

    return BoundingBox(
        west = west - padLng,
        south = south - padLat,
        east = east + padLng,
        north = north + padLat,
    )
}
