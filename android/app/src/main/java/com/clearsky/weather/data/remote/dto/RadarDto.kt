package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RadarResponseDto(
    val generated: Long,
    val host: String,
    val radar: RadarFramesDto,
    val satellite: SatelliteFramesDto? = null,
    val tileUrlTemplate: String
)

@Serializable
data class RadarFramesDto(
    val past: List<RadarFrameDto>,
    val nowcast: List<RadarFrameDto> = emptyList()
)

@Serializable
data class RadarFrameDto(
    val time: Long,
    val path: String
)

@Serializable
data class SatelliteFramesDto(
    val infrared: List<RadarFrameDto> = emptyList()
)
