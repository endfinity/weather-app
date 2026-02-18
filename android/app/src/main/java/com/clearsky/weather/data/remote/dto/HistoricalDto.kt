package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HistoricalResponseDto(
    val location: LocationDto,
    val daily: HistoricalDailyDataDto,
    val hourly: HistoricalHourlyDataDto,
    val units: UnitsDto
)

@Serializable
data class HistoricalDailyDataDto(
    val time: List<String>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val feelsLikeMax: List<Double>,
    val feelsLikeMin: List<Double>,
    val precipitationSum: List<Double>,
    val rainSum: List<Double>,
    val snowfallSum: List<Double>,
    val weatherCode: List<Int>,
    val weatherDescription: List<String>,
    val weatherIcon: List<String>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val sunshineDuration: List<Long>,
    val windSpeedMax: List<Double>,
    val windGustsMax: List<Double>,
    val windDirectionDominant: List<Int>
)

@Serializable
data class HistoricalHourlyDataDto(
    val time: List<String>,
    val temperature: List<Double>,
    val feelsLike: List<Double>,
    val humidity: List<Int>,
    val precipitation: List<Double>,
    val rain: List<Double>,
    val snowfall: List<Double>,
    val weatherCode: List<Int>,
    val weatherDescription: List<String>,
    val weatherIcon: List<String>,
    val cloudCover: List<Int>,
    val windSpeed: List<Double>,
    val windDirection: List<Int>,
    val pressureMsl: List<Double>
)

@Serializable
data class OnThisDayResponseDto(
    val date: String,
    val years: List<YearlyHistoricalDto>
)

@Serializable
data class YearlyHistoricalDto(
    val year: Int,
    val data: HistoricalResponseDto? = null,
    val error: String? = null
)
