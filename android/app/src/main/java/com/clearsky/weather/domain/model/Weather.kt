package com.clearsky.weather.domain.model

data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val timezoneAbbreviation: String,
    val utcOffsetSeconds: Int
)

data class CurrentWeather(
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

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val precipitationProbability: Int,
    val precipitation: Double,
    val rain: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudCover: Int,
    val visibility: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double,
    val uvIndex: Double,
    val pressureMsl: Double,
    val dewPoint: Double,
    val isDay: Boolean
)

data class DailyForecast(
    val date: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val feelsLikeMax: Double,
    val feelsLikeMin: Double,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val rainSum: Double,
    val snowfallSum: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val sunrise: String,
    val sunset: String,
    val sunshineDuration: Long,
    val daylightDuration: Long,
    val uvIndexMax: Double,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val windDirectionDominant: Int,
    val precipitationHours: Int
)

data class Minutely15(
    val time: String,
    val precipitation: Double,
    val rain: Double,
    val snowfall: Double,
    val weatherCode: Int
)

data class WeatherUnits(
    val temperature: String,
    val windSpeed: String,
    val precipitation: String,
    val pressure: String,
    val visibility: String
)

data class WeatherData(
    val location: WeatherLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val minutely15: List<Minutely15>?,
    val minutely15Available: Boolean,
    val units: WeatherUnits,
    val fetchedAt: Long
)
