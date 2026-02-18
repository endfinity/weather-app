package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.repository.PremiumRepository
import javax.inject.Inject

class GetOnThisDayUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        years: Int = 5,
        units: String = "metric"
    ): Result<OnThisDayData> =
        repository.getOnThisDay(latitude, longitude, years, units)
}
