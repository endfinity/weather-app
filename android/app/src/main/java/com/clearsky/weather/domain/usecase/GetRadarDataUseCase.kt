package com.clearsky.weather.domain.usecase

import com.clearsky.weather.domain.model.RadarData
import com.clearsky.weather.domain.repository.PremiumRepository
import javax.inject.Inject

class GetRadarDataUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    suspend operator fun invoke(): Result<RadarData> =
        repository.getRadarFrames()
}
