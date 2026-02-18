package com.clearsky.weather.ui.util

import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.domain.model.WindSpeedUnit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

object FormatUtil {

    fun formatTemperature(
        celsius: Double,
        unit: TemperatureUnit = TemperatureUnit.CELSIUS
    ): String {
        val converted = UnitConverter.convertTemperature(celsius, unit)
        return "${converted.roundToInt()}°"
    }

    fun formatTemperatureValue(
        celsius: Double,
        unit: TemperatureUnit = TemperatureUnit.CELSIUS
    ): String {
        val converted = UnitConverter.convertTemperature(celsius, unit)
        return "${converted.roundToInt()}"
    }

    fun formatHour(
        isoTime: String,
        timeFormat: TimeFormat = TimeFormat.H12
    ): String {
        val dateTime = LocalDateTime.parse(isoTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val pattern = if (timeFormat == TimeFormat.H24) "HH:mm" else "h a"
        return dateTime.format(DateTimeFormatter.ofPattern(pattern))
    }

    fun formatDayOfWeek(isoDate: String): String {
        val date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }

    fun formatDate(isoDate: String): String {
        val date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
        return date.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    fun formatTime(
        isoTime: String,
        timeFormat: TimeFormat = TimeFormat.H12
    ): String {
        val dateTime = LocalDateTime.parse(isoTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val pattern = if (timeFormat == TimeFormat.H24) "HH:mm" else "h:mm a"
        return dateTime.format(DateTimeFormatter.ofPattern(pattern))
    }

    fun formatWindSpeed(
        kmh: Double,
        unit: WindSpeedUnit = WindSpeedUnit.KMH
    ): String {
        val converted = UnitConverter.convertWindSpeed(kmh, unit)
        return "${converted.roundToInt()} ${unit.symbol}"
    }

    fun formatWindSpeedValue(
        kmh: Double,
        unit: WindSpeedUnit = WindSpeedUnit.KMH
    ): String {
        val converted = UnitConverter.convertWindSpeed(kmh, unit)
        return "${converted.roundToInt()}"
    }

    fun formatWindDirection(degrees: Int): String = when {
        degrees in 338..360 || degrees in 0..22 -> "N"
        degrees in 23..67 -> "NE"
        degrees in 68..112 -> "E"
        degrees in 113..157 -> "SE"
        degrees in 158..202 -> "S"
        degrees in 203..247 -> "SW"
        degrees in 248..292 -> "W"
        degrees in 293..337 -> "NW"
        else -> "--"
    }

    fun formatPressure(pressure: Double): String = "${pressure.roundToInt()} hPa"

    fun formatVisibility(meters: Int): String = when {
        meters >= 1000 -> "${meters / 1000} km"
        else -> "$meters m"
    }

    fun formatPercentage(value: Int): String = "$value%"

    fun formatUvIndex(uv: Double): String = String.format("%.1f", uv)

    fun uvIndexLevel(uv: Double): String = when {
        uv <= 2 -> "Low"
        uv <= 5 -> "Moderate"
        uv <= 7 -> "High"
        uv <= 10 -> "Very High"
        else -> "Extreme"
    }

    fun formatPrecipitation(
        mm: Double,
        unit: PrecipitationUnit = PrecipitationUnit.MM
    ): String {
        val converted = UnitConverter.convertPrecipitation(mm, unit)
        return "${String.format("%.1f", converted)} ${unit.symbol}"
    }

    fun formatRelativeTime(epochMs: Long): String {
        val diffMs = System.currentTimeMillis() - epochMs
        val minutes = diffMs / 60_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else -> "${minutes / 1440}d ago"
        }
    }
}
