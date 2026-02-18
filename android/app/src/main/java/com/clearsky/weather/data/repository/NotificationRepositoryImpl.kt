package com.clearsky.weather.data.repository

import com.clearsky.weather.data.notification.DeviceRegistrationRequest
import com.clearsky.weather.data.notification.LocationPayload
import com.clearsky.weather.data.notification.toDomain
import com.clearsky.weather.data.remote.ClearSkyApi
import com.clearsky.weather.domain.model.WeatherAlert
import com.clearsky.weather.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: ClearSkyApi
) : NotificationRepository {

    override suspend fun getActiveAlerts(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherAlert>> = try {
        val response = api.getAlerts(latitude, longitude)
        Result.success(response.data.alerts.map { it.toDomain() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun registerDevice(
        fcmToken: String,
        locations: List<Triple<Double, Double, String>>
    ): Result<Unit> = try {
        api.registerDevice(
            DeviceRegistrationRequest(
                fcmToken = fcmToken,
                locations = locations.map { (lat, lon, name) ->
                    LocationPayload(lat, lon, name)
                }
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDeviceLocations(
        fcmToken: String,
        locations: List<Triple<Double, Double, String>>
    ): Result<Unit> = try {
        api.updateDeviceLocations(
            token = fcmToken,
            request = DeviceRegistrationRequest(
                fcmToken = fcmToken,
                locations = locations.map { (lat, lon, name) ->
                    LocationPayload(lat, lon, name)
                }
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unregisterDevice(fcmToken: String): Result<Unit> = try {
        api.unregisterDevice(fcmToken)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
