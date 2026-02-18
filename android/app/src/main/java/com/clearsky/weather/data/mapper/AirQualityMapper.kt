package com.clearsky.weather.data.mapper

import com.clearsky.weather.data.remote.dto.AirQualityResponseDto
import com.clearsky.weather.data.remote.dto.CurrentAirQualityDto
import com.clearsky.weather.data.remote.dto.HourlyAirQualityDataDto
import com.clearsky.weather.data.remote.dto.HourlyPollenDataDto
import com.clearsky.weather.data.remote.dto.PollenDataDto
import com.clearsky.weather.domain.model.AirQualityData
import com.clearsky.weather.domain.model.AqiCategory
import com.clearsky.weather.domain.model.CurrentAirQuality
import com.clearsky.weather.domain.model.HourlyAirQuality
import com.clearsky.weather.domain.model.HourlyPollen
import com.clearsky.weather.domain.model.PollenData

fun AirQualityResponseDto.toDomain(): AirQualityData = AirQualityData(
    current = current.toDomain(),
    hourly = hourly.toDomainList(),
    pollen = pollen.toDomain(),
    fetchedAt = System.currentTimeMillis()
)

fun CurrentAirQualityDto.toDomain(): CurrentAirQuality = CurrentAirQuality(
    time = time,
    aqi = aqi,
    aqiCategory = AqiCategory.fromAqi(aqi),
    pm25 = pm25,
    pm10 = pm10,
    carbonMonoxide = carbonMonoxide,
    nitrogenDioxide = nitrogenDioxide,
    sulphurDioxide = sulphurDioxide,
    ozone = ozone,
    uvIndex = uvIndex,
    uvIndexClearSky = uvIndexClearSky
)

fun HourlyAirQualityDataDto.toDomainList(): List<HourlyAirQuality> =
    time.indices.map { i ->
        HourlyAirQuality(
            time = time[i],
            aqi = aqi[i],
            pm25 = pm25[i],
            pm10 = pm10[i],
            ozone = ozone[i],
            uvIndex = uvIndex[i]
        )
    }

fun PollenDataDto.toDomain(): PollenData = PollenData(
    available = available,
    hourly = hourly?.toDomainList()
)

fun HourlyPollenDataDto.toDomainList(): List<HourlyPollen> =
    time.indices.map { i ->
        HourlyPollen(
            time = time[i],
            alderPollen = alderPollen[i],
            birchPollen = birchPollen[i],
            grassPollen = grassPollen[i],
            mugwortPollen = mugwortPollen[i],
            olivePollen = olivePollen[i],
            ragweedPollen = ragweedPollen[i]
        )
    }
