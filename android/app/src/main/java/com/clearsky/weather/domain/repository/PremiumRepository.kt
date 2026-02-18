package com.clearsky.weather.domain.repository

import com.clearsky.weather.domain.model.HistoricalWeatherData
import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.model.PremiumStatus
import com.clearsky.weather.domain.model.RadarData
import kotlinx.coroutines.flow.StateFlow

interface PremiumRepository {
    val premiumStatus: StateFlow<PremiumStatus>

    suspend fun checkPremiumStatus(): PremiumStatus

    suspend fun verifyPurchase(purchaseToken: String, productId: String): Result<Boolean>

    suspend fun getHistoricalWeather(
        latitude: Double,
        longitude: Double,
        startDate: String,
        endDate: String,
        units: String = "metric"
    ): Result<HistoricalWeatherData>

    suspend fun getOnThisDay(
        latitude: Double,
        longitude: Double,
        years: Int = 5,
        units: String = "metric"
    ): Result<OnThisDayData>

    suspend fun getRadarFrames(): Result<RadarData>
}
