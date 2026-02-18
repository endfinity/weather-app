package com.clearsky.weather.data.mapper

import com.clearsky.weather.data.remote.dto.HistoricalResponseDto
import com.clearsky.weather.data.remote.dto.OnThisDayResponseDto
import com.clearsky.weather.domain.model.HistoricalDay
import com.clearsky.weather.domain.model.HistoricalHour
import com.clearsky.weather.domain.model.HistoricalWeatherData
import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.model.WeatherLocation
import com.clearsky.weather.domain.model.WeatherUnits
import com.clearsky.weather.domain.model.YearlyHistorical

fun HistoricalResponseDto.toDomain(): HistoricalWeatherData = HistoricalWeatherData(
    location = WeatherLocation(
        latitude = location.latitude,
        longitude = location.longitude,
        elevation = location.elevation,
        timezone = location.timezone,
        timezoneAbbreviation = location.timezoneAbbreviation,
        utcOffsetSeconds = location.utcOffsetSeconds
    ),
    daily = daily.time.indices.map { i ->
        HistoricalDay(
            date = daily.time[i],
            temperatureMax = daily.temperatureMax[i],
            temperatureMin = daily.temperatureMin[i],
            feelsLikeMax = daily.feelsLikeMax[i],
            feelsLikeMin = daily.feelsLikeMin[i],
            precipitationSum = daily.precipitationSum[i],
            rainSum = daily.rainSum[i],
            snowfallSum = daily.snowfallSum[i],
            weatherCode = daily.weatherCode[i],
            weatherDescription = daily.weatherDescription[i],
            weatherIcon = daily.weatherIcon[i],
            sunrise = daily.sunrise[i],
            sunset = daily.sunset[i],
            sunshineDuration = daily.sunshineDuration[i],
            windSpeedMax = daily.windSpeedMax[i],
            windGustsMax = daily.windGustsMax[i],
            windDirectionDominant = daily.windDirectionDominant[i]
        )
    },
    hourly = hourly.time.indices.map { i ->
        HistoricalHour(
            time = hourly.time[i],
            temperature = hourly.temperature[i],
            feelsLike = hourly.feelsLike[i],
            humidity = hourly.humidity[i],
            precipitation = hourly.precipitation[i],
            rain = hourly.rain[i],
            snowfall = hourly.snowfall[i],
            weatherCode = hourly.weatherCode[i],
            weatherDescription = hourly.weatherDescription[i],
            weatherIcon = hourly.weatherIcon[i],
            cloudCover = hourly.cloudCover[i],
            windSpeed = hourly.windSpeed[i],
            windDirection = hourly.windDirection[i],
            pressureMsl = hourly.pressureMsl[i]
        )
    },
    units = WeatherUnits(
        temperature = units.temperature,
        windSpeed = units.windSpeed,
        precipitation = units.precipitation,
        pressure = units.pressure,
        visibility = units.visibility
    )
)

fun OnThisDayResponseDto.toDomain(): OnThisDayData = OnThisDayData(
    date = date,
    years = years.map { yearly ->
        YearlyHistorical(
            year = yearly.year,
            data = yearly.data?.toDomain(),
            error = yearly.error
        )
    }
)
