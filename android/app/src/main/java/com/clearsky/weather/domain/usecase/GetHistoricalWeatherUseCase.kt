package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.HistoricalWeatherData
import com.clearsky.weather.domain.repository.PremiumRepository
import javax.inject.Inject

class GetHistoricalWeatherUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        startDate: String,
        endDate: String,
        units: String = "metric"
    ): Result<HistoricalWeatherData> =
        repository.getHistoricalWeather(latitude, longitude, startDate, endDate, units)
}
