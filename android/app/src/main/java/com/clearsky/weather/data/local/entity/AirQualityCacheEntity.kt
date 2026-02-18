package com.clearsky.weather.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "air_quality_cache",
    indices = [Index(value = ["fetchedAt"])]
)
data class AirQualityCacheEntity(
    @PrimaryKey
    val locationKey: String,
    val airQualityJson: String,
    val fetchedAt: Long
)
