package com.clearsky.weather.data.mapper

import com.clearsky.weather.data.local.entity.SavedLocationEntity
import com.clearsky.weather.data.remote.dto.GeocodingResultDto
import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.model.SavedLocation

fun GeocodingResultDto.toDomain(): GeocodingResult = GeocodingResult(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    elevation = elevation,
    timezone = timezone,
    country = country,
    countryCode = countryCode,
    admin1 = admin1,
    admin2 = admin2,
    population = population,
    featureCode = featureCode
)

fun SavedLocationEntity.toDomain(): SavedLocation = SavedLocation(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    countryCode = countryCode,
    admin1 = admin1,
    timezone = timezone,
    isCurrentLocation = isCurrentLocation,
    sortOrder = sortOrder,
    addedAt = addedAt
)

fun SavedLocation.toEntity(): SavedLocationEntity = SavedLocationEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    countryCode = countryCode,
    admin1 = admin1,
    timezone = timezone,
    isCurrentLocation = isCurrentLocation,
    sortOrder = sortOrder,
    addedAt = addedAt
)
