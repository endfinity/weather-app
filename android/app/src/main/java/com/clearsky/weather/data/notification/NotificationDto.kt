package com.clearsky.weather.data.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("platform") val platform: String = "android",
    @SerialName("locations") val locations: List<LocationPayload> = emptyList()
)

@Serializable
data class LocationPayload(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("name") val name: String
)

@Serializable
data class DeviceRegistrationResponse(
    @SerialName("id") val id: String,
    @SerialName("message") val message: String
)

@Serializable
data class AlertsResponse(
    @SerialName("alerts") val alerts: List<AlertDto> = emptyList()
)

@Serializable
data class AlertDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("severity") val severity: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("location_name") val locationName: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("issued_at") val issuedAt: String
)
