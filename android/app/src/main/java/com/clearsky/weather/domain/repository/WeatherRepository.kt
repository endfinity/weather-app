package com.clearsky.weather.domain.repository

import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.WeatherData

interface WeatherRepository {
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        units: String = "metric",
        forceRefresh: Boolean = false
    ): Result<WeatherData>

    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Result<AirQualityData>

    suspend fun getCombinedWeather(
        latitude: Double,
        longitude: Double,
        units: String = "metric",
        forceRefresh: Boolean = false
    ): Result<Pair<WeatherData, AirQualityData>>

    suspend fun clearCache()
}
