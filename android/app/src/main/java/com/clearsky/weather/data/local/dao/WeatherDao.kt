package com.clearsky.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.clearsky.weather.data.local.entity.AirQualityCacheEntity
import com.clearsky.weather.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    suspend fun getWeatherCache(key: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    fun observeWeatherCache(key: String): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE locationKey = :key")
    suspend fun deleteWeatherCache(key: String)

    @Query("DELETE FROM weather_cache WHERE fetchedAt < :timestamp")
    suspend fun deleteStaleWeatherCache(timestamp: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun deleteAllWeatherCache()

    @Query("SELECT * FROM air_quality_cache WHERE locationKey = :key")
    suspend fun getAirQualityCache(key: String): AirQualityCacheEntity?

    @Query("SELECT * FROM air_quality_cache WHERE locationKey = :key")
    fun observeAirQualityCache(key: String): Flow<AirQualityCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirQualityCache(cache: AirQualityCacheEntity)

    @Query("DELETE FROM air_quality_cache WHERE locationKey = :key")
    suspend fun deleteAirQualityCache(key: String)

    @Query("DELETE FROM air_quality_cache WHERE fetchedAt < :timestamp")
    suspend fun deleteStaleAirQualityCache(timestamp: Long)

    @Query("DELETE FROM air_quality_cache")
    suspend fun deleteAllAirQualityCache()

    @Transaction
    suspend fun deleteAllCache() {
        deleteAllWeatherCache()
        deleteAllAirQualityCache()
    }

    @Transaction
    suspend fun deleteStaleCache(timestamp: Long) {
        deleteStaleWeatherCache(timestamp)
        deleteStaleAirQualityCache(timestamp)
    }
}
