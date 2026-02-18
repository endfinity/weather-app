package com.clearsky.weather.ui.util

import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.WindSpeedUnit

object UnitConverter {

    fun convertTemperature(celsius: Double, unit: TemperatureUnit): Double = when (unit) {
        TemperatureUnit.CELSIUS -> celsius
        TemperatureUnit.FAHRENHEIT -> celsius * 9.0 / 5.0 + 32.0
    }

    fun convertWindSpeed(kmh: Double, unit: WindSpeedUnit): Double = when (unit) {
        WindSpeedUnit.KMH -> kmh
        WindSpeedUnit.MPH -> kmh * 0.621371
        WindSpeedUnit.MS -> kmh / 3.6
        WindSpeedUnit.KNOTS -> kmh * 0.539957
    }

    fun convertPrecipitation(mm: Double, unit: PrecipitationUnit): Double = when (unit) {
        PrecipitationUnit.MM -> mm
        PrecipitationUnit.INCH -> mm / 25.4
    }

    fun temperatureSymbol(unit: TemperatureUnit): String = when (unit) {
        TemperatureUnit.CELSIUS -> "°"
        TemperatureUnit.FAHRENHEIT -> "°"
    }

    fun windSpeedLabel(unit: WindSpeedUnit): String = unit.symbol

    fun precipitationLabel(unit: PrecipitationUnit): String = unit.symbol
}
