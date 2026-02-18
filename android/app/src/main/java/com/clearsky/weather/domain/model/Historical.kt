package com.clearsky.weather.domain.model

data class HistoricalDay(
    val date: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val feelsLikeMax: Double,
    val feelsLikeMin: Double,
    val precipitationSum: Double,
    val rainSum: Double,
    val snowfallSum: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val sunrise: String,
    val sunset: String,
    val sunshineDuration: Long,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val windDirectionDominant: Int
)

data class HistoricalHour(
    val time: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val precipitation: Double,
    val rain: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudCover: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val pressureMsl: Double
)

data class HistoricalWeatherData(
    val location: WeatherLocation,
    val daily: List<HistoricalDay>,
    val hourly: List<HistoricalHour>,
    val units: WeatherUnits
)

data class OnThisDayData(
    val date: String,
    val years: List<YearlyHistorical>
)

data class YearlyHistorical(
    val year: Int,
    val data: HistoricalWeatherData?,
    val error: String? = null
)
