package com.clearsky.weather.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.clearsky.weather.data.local.dao.LocationDao
import com.clearsky.weather.data.local.dao.WeatherDao
import com.clearsky.weather.data.local.entity.AirQualityCacheEntity
import com.clearsky.weather.data.local.entity.SavedLocationEntity
import com.clearsky.weather.data.local.entity.WeatherCacheEntity

@Database(
    entities = [
        SavedLocationEntity::class,
        WeatherCacheEntity::class,
        AirQualityCacheEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class ClearSkyDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun weatherDao(): WeatherDao
}
