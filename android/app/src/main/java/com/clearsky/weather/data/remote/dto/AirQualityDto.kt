package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AirQualityResponseDto(
    val current: CurrentAirQualityDto,
    val hourly: HourlyAirQualityDataDto,
    val pollen: PollenDataDto
)

@Serializable
data class CurrentAirQualityDto(
    val time: String,
    val aqi: Int,
    val aqiCategory: String,
    val pm25: Double,
    val pm10: Double,
    val carbonMonoxide: Double,
    val nitrogenDioxide: Double,
    val sulphurDioxide: Double,
    val ozone: Double,
    val uvIndex: Double,
    val uvIndexClearSky: Double
)

@Serializable
data class HourlyAirQualityDataDto(
    val time: List<String>,
    val aqi: List<Int>,
    val pm25: List<Double>,
    val pm10: List<Double>,
    val ozone: List<Double>,
    val uvIndex: List<Double>
)

@Serializable
data class PollenDataDto(
    val available: Boolean,
    val hourly: HourlyPollenDataDto? = null,
    val note: String? = null
)

@Serializable
data class HourlyPollenDataDto(
    val time: List<String>,
    val alderPollen: List<Double>,
    val birchPollen: List<Double>,
    val grassPollen: List<Double>,
    val mugwortPollen: List<Double>,
    val olivePollen: List<Double>,
    val ragweedPollen: List<Double>
)
