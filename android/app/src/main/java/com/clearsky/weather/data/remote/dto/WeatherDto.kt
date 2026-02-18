package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val location: LocationDto,
    val current: CurrentWeatherDto,
    val hourly: HourlyDataDto,
    val daily: DailyDataDto,
    val minutely15: Minutely15DataDto? = null,
    val units: UnitsDto
)

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val timezoneAbbreviation: String,
    val utcOffsetSeconds: Int
)

@Serializable
data class CurrentWeatherDto(
    val time: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val isDay: Boolean,
    val precipitation: Double,
    val rain: Double,
    val showers: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudCover: Int,
    val pressureMsl: Double,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double
)

@Serializable
data class HourlyDataDto(
    val time: List<String>,
    val temperature: List<Double>,
    val feelsLike: List<Double>,
    val humidity: List<Int>,
    val precipitationProbability: List<Int>,
    val precipitation: List<Double>,
    val rain: List<Double>,
    val snowfall: List<Double>,
    val weatherCode: List<Int>,
    val weatherDescription: List<String>,
    val weatherIcon: List<String>,
    val cloudCover: List<Int>,
    val visibility: List<Int>,
    val windSpeed: List<Double>,
    val windDirection: List<Int>,
    val windGusts: List<Double>,
    val uvIndex: List<Double>,
    val pressureMsl: List<Double>,
    val dewPoint: List<Double>,
    val isDay: List<Boolean>
)

@Serializable
data class DailyDataDto(
    val time: List<String>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val feelsLikeMax: List<Double>,
    val feelsLikeMin: List<Double>,
    val precipitationSum: List<Double>,
    val precipitationProbabilityMax: List<Int>,
    val rainSum: List<Double>,
    val snowfallSum: List<Double>,
    val weatherCode: List<Int>,
    val weatherDescription: List<String>,
    val weatherIcon: List<String>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val sunshineDuration: List<Long>,
    val daylightDuration: List<Long>,
    val uvIndexMax: List<Double>,
    val windSpeedMax: List<Double>,
    val windGustsMax: List<Double>,
    val windDirectionDominant: List<Int>,
    val precipitationHours: List<Int>
)

@Serializable
data class Minutely15DataDto(
    val available: Boolean,
    val time: List<String> = emptyList(),
    val precipitation: List<Double> = emptyList(),
    val rain: List<Double> = emptyList(),
    val snowfall: List<Double> = emptyList(),
    val weatherCode: List<Int> = emptyList()
)

@Serializable
data class UnitsDto(
    val temperature: String,
    val windSpeed: String,
    val precipitation: String,
    val pressure: String,
    val visibility: String
)
