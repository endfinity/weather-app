package com.clearsky.weather.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class UserPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MM,
    val timeFormat: TimeFormat = TimeFormat.H12,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val severeAlertsEnabled: Boolean = true,
    val precipitationAlertsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val refreshIntervalMinutes: Int = 30,
    val defaultLocationId: Long? = null
)

enum class TemperatureUnit(val label: String, val symbol: String) {
    CELSIUS("Celsius", "C"),
    FAHRENHEIT("Fahrenheit", "F")
}

enum class WindSpeedUnit(val label: String, val symbol: String) {
    KMH("km/h", "km/h"),
    MPH("mph", "mph"),
    MS("m/s", "m/s"),
    KNOTS("Knots", "kn")
}

enum class PrecipitationUnit(val label: String, val symbol: String) {
    MM("Millimeters", "mm"),
    INCH("Inches", "in")
}

enum class TimeFormat(val label: String) {
    H12("12-hour"),
    H24("24-hour")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}
