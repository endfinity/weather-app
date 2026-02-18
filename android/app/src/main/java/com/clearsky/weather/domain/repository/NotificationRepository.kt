package com.clearsky.weather.domain.repository

import com.clearsky.weather.domain.model.WeatherAlert

interface NotificationRepository {
    suspend fun getActiveAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>>
    suspend fun registerDevice(fcmToken: String, locations: List<Triple<Double, Double, String>>): Result<Unit>
    suspend fun updateDeviceLocations(fcmToken: String, locations: List<Triple<Double, Double, String>>): Result<Unit>
    suspend fun unregisterDevice(fcmToken: String): Result<Unit>
}
