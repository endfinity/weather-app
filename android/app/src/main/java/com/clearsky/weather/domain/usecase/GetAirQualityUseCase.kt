package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Result<AirQualityData> = repository.getAirQuality(latitude, longitude, forceRefresh)
}
