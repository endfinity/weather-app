package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto>
)

@Serializable
data class GeocodingResultDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val timezone: String,
    val country: String,
    val countryCode: String,
    val admin1: String? = null,
    val admin2: String? = null,
    val population: Int? = null,
    val featureCode: String? = null
)
