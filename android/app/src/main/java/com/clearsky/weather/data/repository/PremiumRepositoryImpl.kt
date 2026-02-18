package com.clearsky.weather.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.clearsky.weather.data.mapper.toDomain
import com.clearsky.weather.data.remote.ClearSkyApi
import com.clearsky.weather.data.remote.dto.PremiumVerifyRequest
import com.clearsky.weather.domain.model.HistoricalWeatherData
import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.model.PremiumStatus
import com.clearsky.weather.domain.model.RadarData
import com.clearsky.weather.domain.repository.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "clearsky_premium")

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val api: ClearSkyApi,
    @ApplicationContext private val context: Context
) : PremiumRepository {

    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        val PRODUCT_ID = stringPreferencesKey("product_id")
    }

    private val _premiumStatus = MutableStateFlow(PremiumStatus(isPremium = false))
    override val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    private suspend fun getStoredToken(): String? =
        context.premiumDataStore.data.map { it[Keys.PURCHASE_TOKEN] }.first()

    private fun authHeader(token: String): String = "Bearer $token"

    override suspend fun checkPremiumStatus(): PremiumStatus {
        val storedToken = getStoredToken()
        if (storedToken != null) {
            return try {
                val response = api.getPremiumStatus(getDeviceId())
                val status = PremiumStatus(
                    isPremium = response.data.premium,
                    productId = response.data.productId,
                    purchaseTime = response.data.purchaseTime
                )
                _premiumStatus.value = status
                status
            } catch (_: Exception) {
                val localStatus = PremiumStatus(isPremium = true, productId = "clearsky_premium")
                _premiumStatus.value = localStatus
                localStatus
            }
        }
        val status = PremiumStatus(isPremium = false)
        _premiumStatus.value = status
        return status
    }

    override suspend fun verifyPurchase(purchaseToken: String, productId: String): Result<Boolean> =
        runCatching {
            val request = PremiumVerifyRequest(
                deviceId = getDeviceId(),
                purchaseToken = purchaseToken,
                productId = productId
            )
            val response = api.verifyPremiumPurchase(request)

            if (response.data.premium) {
                context.premiumDataStore.edit { prefs ->
                    prefs[Keys.IS_PREMIUM] = true
                    prefs[Keys.PURCHASE_TOKEN] = purchaseToken
                    prefs[Keys.PRODUCT_ID] = productId
                }
                _premiumStatus.value = PremiumStatus(isPremium = true, productId = productId)
            }
            response.data.premium
        }

    override suspend fun getHistoricalWeather(
        latitude: Double,
        longitude: Double,
        startDate: String,
        endDate: String,
        units: String
    ): Result<HistoricalWeatherData> = runCatching {
        val token = getStoredToken() ?: throw IllegalStateException("Premium required")
        val response = api.getHistoricalWeather(
            authToken = authHeader(token),
            latitude = latitude,
            longitude = longitude,
            startDate = startDate,
            endDate = endDate,
            units = units
        )
        response.data.toDomain()
    }

    override suspend fun getOnThisDay(
        latitude: Double,
        longitude: Double,
        years: Int,
        units: String
    ): Result<OnThisDayData> = runCatching {
        val token = getStoredToken() ?: throw IllegalStateException("Premium required")
        val response = api.getOnThisDay(
            authToken = authHeader(token),
            latitude = latitude,
            longitude = longitude,
            years = years,
            units = units
        )
        response.data.toDomain()
    }

    override suspend fun getRadarFrames(): Result<RadarData> = runCatching {
        val token = getStoredToken() ?: throw IllegalStateException("Premium required")
        val response = api.getRadarFrames(authToken = authHeader(token))
        response.data.toDomain()
    }
}
