package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CombinedResponseDto(
    val weather: WeatherResponseDto,
    val airQuality: AirQualityResponseDto
)
