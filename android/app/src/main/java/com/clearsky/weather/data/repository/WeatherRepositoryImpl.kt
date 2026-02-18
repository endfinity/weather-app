package com.clearsky.weather.data.repository

import com.clearsky.weather.data.local.dao.WeatherDao
import com.clearsky.weather.data.local.entity.AirQualityCacheEntity
import com.clearsky.weather.data.local.entity.WeatherCacheEntity
import com.clearsky.weather.data.mapper.toDomain
import com.clearsky.weather.data.remote.ClearSkyApi
import com.clearsky.weather.data.remote.dto.AirQualityResponseDto
import com.clearsky.weather.data.remote.dto.WeatherResponseDto
import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.repository.WeatherRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: ClearSkyApi,
    private val weatherDao: WeatherDao,
    private val json: Json
) : WeatherRepository {

    companion object {
        private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }

    private fun locationKey(lat: Double, lon: Double): String =
        "${String.format("%.4f", lat)}_${String.format("%.4f", lon)}"

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        units: String,
        forceRefresh: Boolean
    ): Result<WeatherData> = runCatching {
        val key = locationKey(latitude, longitude)

        if (!forceRefresh) {
            val cached = weatherDao.getWeatherCache(key)
            if (cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_DURATION_MS) {
                val dto = json.decodeFromString<WeatherResponseDto>(cached.weatherJson)
                return@runCatching dto.toDomain().copy(fetchedAt = cached.fetchedAt)
            }
        }

        val response = api.getWeather(latitude, longitude, units)
        val weatherData = response.data.toDomain()

        weatherDao.insertWeatherCache(
            WeatherCacheEntity(
                locationKey = key,
                weatherJson = json.encodeToString(response.data),
                fetchedAt = weatherData.fetchedAt
            )
        )

        weatherData
    }

    override suspend fun getAirQuality(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean
    ): Result<AirQualityData> = runCatching {
        val key = locationKey(latitude, longitude)

        if (!forceRefresh) {
            val cached = weatherDao.getAirQualityCache(key)
            if (cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_DURATION_MS) {
                val dto = json.decodeFromString<AirQualityResponseDto>(cached.airQualityJson)
                return@runCatching dto.toDomain().copy(fetchedAt = cached.fetchedAt)
            }
        }

        val response = api.getAirQuality(latitude, longitude)
        val airQualityData = response.data.toDomain()

        weatherDao.insertAirQualityCache(
            AirQualityCacheEntity(
                locationKey = key,
                airQualityJson = json.encodeToString(response.data),
                fetchedAt = airQualityData.fetchedAt
            )
        )

        airQualityData
    }

    override suspend fun getCombinedWeather(
        latitude: Double,
        longitude: Double,
        units: String,
        forceRefresh: Boolean
    ): Result<Pair<WeatherData, AirQualityData>> = runCatching {
        val response = api.getCombinedWeather(latitude, longitude, units)
        val weatherData = response.data.weather.toDomain()
        val airQualityData = response.data.airQuality.toDomain()

        val key = locationKey(latitude, longitude)
        weatherDao.insertWeatherCache(
            WeatherCacheEntity(
                locationKey = key,
                weatherJson = json.encodeToString(response.data.weather),
                fetchedAt = weatherData.fetchedAt
            )
        )
        weatherDao.insertAirQualityCache(
            AirQualityCacheEntity(
                locationKey = key,
                airQualityJson = json.encodeToString(response.data.airQuality),
                fetchedAt = airQualityData.fetchedAt
            )
        )

        Pair(weatherData, airQualityData)
    }

    override suspend fun clearCache() {
        val staleTimestamp = System.currentTimeMillis() - CACHE_DURATION_MS
        weatherDao.deleteStaleCache(staleTimestamp)
    }
}
