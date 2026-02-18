package com.clearsky.weather.domain.model

data class WeatherAlert(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val description: String,
    val locationName: String,
    val startTime: String,
    val endTime: String?,
    val issuedAt: String
)

enum class AlertType {
    THUNDERSTORM, EXTREME_TEMP, HIGH_WIND, HEAVY_PRECIPITATION,
    HIGH_UV, POOR_AIR_QUALITY, SNOWSTORM, FREEZING_RAIN
}

enum class AlertSeverity {
    MINOR, MODERATE, SEVERE, EXTREME
}
