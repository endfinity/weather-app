package com.clearsky.weather.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather_cache",
    indices = [Index(value = ["fetchedAt"])]
)
data class WeatherCacheEntity(
    @PrimaryKey
    val locationKey: String,
    val weatherJson: String,
    val fetchedAt: Long
)
