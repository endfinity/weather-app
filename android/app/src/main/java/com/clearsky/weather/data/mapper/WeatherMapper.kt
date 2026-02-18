package com.clearsky.weather.data.mapper

import com.clearsky.weather.data.remote.dto.CurrentWeatherDto
import com.clearsky.weather.data.remote.dto.DailyDataDto
import com.clearsky.weather.data.remote.dto.HourlyDataDto
import com.clearsky.weather.data.remote.dto.LocationDto
import com.clearsky.weather.data.remote.dto.Minutely15DataDto
import com.clearsky.weather.data.remote.dto.UnitsDto
import com.clearsky.weather.data.remote.dto.WeatherResponseDto
import com.clearsky.weather.domain.model.CurrentWeather
import com.clearsky.weather.domain.model.DailyForecast
import com.clearsky.weather.domain.model.HourlyForecast
import com.clearsky.weather.domain.model.Minutely15
import com.clearsky.weather.domain.model.WeatherData
import com.clearsky.weather.domain.model.WeatherLocation
import com.clearsky.weather.domain.model.WeatherUnits

fun WeatherResponseDto.toDomain(): WeatherData = WeatherData(
    location = location.toDomain(),
    current = current.toDomain(),
    hourly = hourly.toDomainList(),
    daily = daily.toDomainList(),
    minutely15 = minutely15?.toDomainList(),
    minutely15Available = minutely15?.available ?: false,
    units = units.toDomain(),
    fetchedAt = System.currentTimeMillis()
)

fun LocationDto.toDomain(): WeatherLocation = WeatherLocation(
    latitude = latitude,
    longitude = longitude,
    elevation = elevation,
    timezone = timezone,
    timezoneAbbreviation = timezoneAbbreviation,
    utcOffsetSeconds = utcOffsetSeconds
)

fun CurrentWeatherDto.toDomain(): CurrentWeather = CurrentWeather(
    time = time,
    temperature = temperature,
    feelsLike = feelsLike,
    humidity = humidity,
    isDay = isDay,
    precipitation = precipitation,
    rain = rain,
    showers = showers,
    snowfall = snowfall,
    weatherCode = weatherCode,
    weatherDescription = weatherDescription,
    weatherIcon = weatherIcon,
    cloudCover = cloudCover,
    pressureMsl = pressureMsl,
    surfacePressure = surfacePressure,
    windSpeed = windSpeed,
    windDirection = windDirection,
    windGusts = windGusts
)

fun HourlyDataDto.toDomainList(): List<HourlyForecast> =
    time.indices.map { i ->
        HourlyForecast(
            time = time[i],
            temperature = temperature[i],
            feelsLike = feelsLike[i],
            humidity = humidity[i],
            precipitationProbability = precipitationProbability[i],
            precipitation = precipitation[i],
            rain = rain[i],
            snowfall = snowfall[i],
            weatherCode = weatherCode[i],
            weatherDescription = weatherDescription[i],
            weatherIcon = weatherIcon[i],
            cloudCover = cloudCover[i],
            visibility = visibility[i],
            windSpeed = windSpeed[i],
            windDirection = windDirection[i],
            windGusts = windGusts[i],
            uvIndex = uvIndex[i],
            pressureMsl = pressureMsl[i],
            dewPoint = dewPoint[i],
            isDay = isDay[i]
        )
    }

fun DailyDataDto.toDomainList(): List<DailyForecast> =
    time.indices.map { i ->
        DailyForecast(
            date = time[i],
            temperatureMax = temperatureMax[i],
            temperatureMin = temperatureMin[i],
            feelsLikeMax = feelsLikeMax[i],
            feelsLikeMin = feelsLikeMin[i],
            precipitationSum = precipitationSum[i],
            precipitationProbabilityMax = precipitationProbabilityMax[i],
            rainSum = rainSum[i],
            snowfallSum = snowfallSum[i],
            weatherCode = weatherCode[i],
            weatherDescription = weatherDescription[i],
            weatherIcon = weatherIcon[i],
            sunrise = sunrise[i],
            sunset = sunset[i],
            sunshineDuration = sunshineDuration[i],
            daylightDuration = daylightDuration[i],
            uvIndexMax = uvIndexMax[i],
            windSpeedMax = windSpeedMax[i],
            windGustsMax = windGustsMax[i],
            windDirectionDominant = windDirectionDominant[i],
            precipitationHours = precipitationHours[i]
        )
    }

fun Minutely15DataDto.toDomainList(): List<Minutely15> =
    time.indices.map { i ->
        Minutely15(
            time = time[i],
            precipitation = precipitation[i],
            rain = rain[i],
            snowfall = snowfall[i],
            weatherCode = weatherCode[i]
        )
    }

fun UnitsDto.toDomain(): WeatherUnits = WeatherUnits(
    temperature = temperature,
    windSpeed = windSpeed,
    precipitation = precipitation,
    pressure = pressure,
    visibility = visibility
)
