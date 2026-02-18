package com.clearsky.weather.domain.model

data class RadarData(
    val generated: Long,
    val host: String,
    val pastFrames: List<RadarFrame>,
    val nowcastFrames: List<RadarFrame>,
    val tileUrlTemplate: String
)

data class RadarFrame(
    val time: Long,
    val path: String
) {
    fun buildTileUrl(host: String, size: Int = 256, z: Int = 3, x: Int = 0, y: Int = 0, color: Int = 6, smooth: Int = 1, snow: Int = 1): String =
        "$host$path/$size/$z/$x/$y/$color/${smooth}_$snow.png"
}
