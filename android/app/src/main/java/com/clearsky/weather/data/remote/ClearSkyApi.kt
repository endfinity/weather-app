package com.clearsky.weather.data.remote

import com.clearsky.weather.data.notification.AlertsResponse
import com.clearsky.weather.data.notification.DeviceRegistrationRequest
import com.clearsky.weather.data.notification.DeviceRegistrationResponse
import com.clearsky.weather.data.remote.dto.AirQualityResponseDto
import com.clearsky.weather.data.remote.dto.ApiEnvelope
import com.clearsky.weather.data.remote.dto.CombinedResponseDto
import com.clearsky.weather.data.remote.dto.GeocodingResponseDto
import com.clearsky.weather.data.remote.dto.HistoricalResponseDto
import com.clearsky.weather.data.remote.dto.OnThisDayResponseDto
import com.clearsky.weather.data.remote.dto.PremiumStatusResponseDto
import com.clearsky.weather.data.remote.dto.PremiumVerifyRequest
import com.clearsky.weather.data.remote.dto.PremiumVerifyResponseDto
import com.clearsky.weather.data.remote.dto.RadarResponseDto
import com.clearsky.weather.data.remote.dto.WeatherResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClearSkyApi {

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("timezone") timezone: String = "auto"
    ): ApiEnvelope<WeatherResponseDto>

    @GET("air-quality")
    suspend fun getAirQuality(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): ApiEnvelope<AirQualityResponseDto>

    @GET("geocoding/search")
    suspend fun searchLocations(
        @Query("query") query: String,
        @Query("count") count: Int = 10,
        @Query("lang") language: String = "en"
    ): ApiEnvelope<GeocodingResponseDto>

    @GET("weather/combined")
    suspend fun getCombinedWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("timezone") timezone: String = "auto"
    ): ApiEnvelope<CombinedResponseDto>

    @POST("devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequest
    ): ApiEnvelope<DeviceRegistrationResponse>

    @PUT("devices/{token}")
    suspend fun updateDeviceLocations(
        @Path("token") token: String,
        @Body request: DeviceRegistrationRequest
    ): ApiEnvelope<DeviceRegistrationResponse>

    @DELETE("devices/{token}")
    suspend fun unregisterDevice(
        @Path("token") token: String
    ): ApiEnvelope<DeviceRegistrationResponse>

    @GET("alerts")
    suspend fun getAlerts(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): ApiEnvelope<AlertsResponse>

    // Premium endpoints

    @GET("historical")
    suspend fun getHistoricalWeather(
        @Header("Authorization") authToken: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("units") units: String = "metric",
        @Query("timezone") timezone: String = "auto"
    ): ApiEnvelope<HistoricalResponseDto>

    @GET("historical/on-this-day")
    suspend fun getOnThisDay(
        @Header("Authorization") authToken: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("years") years: Int = 5,
        @Query("units") units: String = "metric",
        @Query("timezone") timezone: String = "auto"
    ): ApiEnvelope<OnThisDayResponseDto>

    @GET("radar")
    suspend fun getRadarFrames(
        @Header("Authorization") authToken: String
    ): ApiEnvelope<RadarResponseDto>

    @POST("premium/verify")
    suspend fun verifyPremiumPurchase(
        @Body request: PremiumVerifyRequest
    ): ApiEnvelope<PremiumVerifyResponseDto>

    @GET("premium/status")
    suspend fun getPremiumStatus(
        @Query("device_id") deviceId: String
    ): ApiEnvelope<PremiumStatusResponseDto>
}
