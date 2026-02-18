package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCombinedWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        units: String = "metric",
        forceRefresh: Boolean = false
    ): Result<Pair<WeatherData, AirQualityData>> =
        repository.getCombinedWeather(latitude, longitude, units, forceRefresh)
}
