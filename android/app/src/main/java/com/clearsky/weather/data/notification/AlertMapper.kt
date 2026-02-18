package com.clearsky.weather.data.notification

import com.clearsky.weather.domain.model.AlertSeverity
import com.clearsky.weather.domain.model.AlertType
import com.clearsky.weather.domain.model.WeatherAlert

fun AlertDto.toDomain(): WeatherAlert = WeatherAlert(
    id = id,
    type = type.toAlertType(),
    severity = severity.toAlertSeverity(),
    title = title,
    description = description,
    locationName = locationName,
    startTime = startTime,
    endTime = endTime,
    issuedAt = issuedAt
)

private fun String.toAlertType(): AlertType = try {
    AlertType.valueOf(uppercase())
} catch (_: IllegalArgumentException) {
    AlertType.THUNDERSTORM
}

private fun String.toAlertSeverity(): AlertSeverity = try {
    AlertSeverity.valueOf(uppercase())
} catch (_: IllegalArgumentException) {
    AlertSeverity.MINOR
}
