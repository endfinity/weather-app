package com.clearsky.weather.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class GeocodingResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val timezone: String,
    val country: String,
    val countryCode: String,
    val admin1: String?,
    val admin2: String?,
    val population: Int?,
    val featureCode: String?
)

@Immutable
data class SavedLocation(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val admin1: String?,
    val timezone: String,
    val isCurrentLocation: Boolean,
    val sortOrder: Int,
    val addedAt: Long
)
