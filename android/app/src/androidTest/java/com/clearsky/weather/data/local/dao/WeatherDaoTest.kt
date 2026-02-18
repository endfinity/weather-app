package com.clearsky.weather.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clearsky.weather.data.local.ClearSkyDatabase
import com.clearsky.weather.data.local.entity.AirQualityCacheEntity
import com.clearsky.weather.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {

    private lateinit var database: ClearSkyDatabase
    private lateinit var weatherDao: WeatherDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ClearSkyDatabase::class.java
        ).allowMainThreadQueries().build()
        weatherDao = database.weatherDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetWeatherCache() = runTest {
        val entity = WeatherCacheEntity(
            locationKey = "40.7128_-74.0060",
            weatherJson = """{"temp":72}""",
            fetchedAt = System.currentTimeMillis()
        )

        weatherDao.insertWeatherCache(entity)
        val result = weatherDao.getWeatherCache("40.7128_-74.0060")

        assertNotNull(result)
        assertEquals(entity.locationKey, result!!.locationKey)
        assertEquals(entity.weatherJson, result.weatherJson)
    }

    @Test
    fun getWeatherCache_returnsNullWhenNotFound() = runTest {
        val result = weatherDao.getWeatherCache("nonexistent_key")
        assertNull(result)
    }

    @Test
    fun insertWeatherCache_replacesOnConflict() = runTest {
        val key = "40.7128_-74.0060"
        val original = WeatherCacheEntity(key, """{"temp":72}""", 1000L)
        val updated = WeatherCacheEntity(key, """{"temp":80}""", 2000L)

        weatherDao.insertWeatherCache(original)
        weatherDao.insertWeatherCache(updated)

        val result = weatherDao.getWeatherCache(key)
        assertNotNull(result)
        assertEquals("""{"temp":80}""", result!!.weatherJson)
        assertEquals(2000L, result.fetchedAt)
    }

    @Test
    fun deleteWeatherCache_removesByKey() = runTest {
        val entity = WeatherCacheEntity("key1", """{"temp":72}""", 1000L)
        weatherDao.insertWeatherCache(entity)

        weatherDao.deleteWeatherCache("key1")

        assertNull(weatherDao.getWeatherCache("key1"))
    }

    @Test
    fun deleteStaleWeatherCache_removesOldEntries() = runTest {
        val fresh = WeatherCacheEntity("fresh", """{"temp":72}""", 2000L)
        val stale = WeatherCacheEntity("stale", """{"temp":60}""", 500L)

        weatherDao.insertWeatherCache(fresh)
        weatherDao.insertWeatherCache(stale)

        weatherDao.deleteStaleWeatherCache(1000L)

        assertNotNull(weatherDao.getWeatherCache("fresh"))
        assertNull(weatherDao.getWeatherCache("stale"))
    }

    @Test
    fun deleteAllWeatherCache_removesAllEntries() = runTest {
        weatherDao.insertWeatherCache(WeatherCacheEntity("k1", "{}", 1000L))
        weatherDao.insertWeatherCache(WeatherCacheEntity("k2", "{}", 2000L))

        weatherDao.deleteAllWeatherCache()

        assertNull(weatherDao.getWeatherCache("k1"))
        assertNull(weatherDao.getWeatherCache("k2"))
    }

    @Test
    fun observeWeatherCache_emitsUpdates() = runTest {
        val key = "40.7128_-74.0060"

        val initial = weatherDao.observeWeatherCache(key).first()
        assertNull(initial)

        val entity = WeatherCacheEntity(key, """{"temp":72}""", 1000L)
        weatherDao.insertWeatherCache(entity)

        val updated = weatherDao.observeWeatherCache(key).first()
        assertNotNull(updated)
        assertEquals("""{"temp":72}""", updated!!.weatherJson)
    }

    @Test
    fun insertAndGetAirQualityCache() = runTest {
        val entity = AirQualityCacheEntity(
            locationKey = "40.7128_-74.0060",
            airQualityJson = """{"aqi":42}""",
            fetchedAt = System.currentTimeMillis()
        )

        weatherDao.insertAirQualityCache(entity)
        val result = weatherDao.getAirQualityCache("40.7128_-74.0060")

        assertNotNull(result)
        assertEquals(entity.airQualityJson, result!!.airQualityJson)
    }

    @Test
    fun deleteAllAirQualityCache_removesAllEntries() = runTest {
        weatherDao.insertAirQualityCache(AirQualityCacheEntity("k1", "{}", 1000L))
        weatherDao.insertAirQualityCache(AirQualityCacheEntity("k2", "{}", 2000L))

        weatherDao.deleteAllAirQualityCache()

        assertNull(weatherDao.getAirQualityCache("k1"))
        assertNull(weatherDao.getAirQualityCache("k2"))
    }

    @Test
    fun deleteAllCache_removesBothWeatherAndAirQuality() = runTest {
        weatherDao.insertWeatherCache(WeatherCacheEntity("w1", "{}", 1000L))
        weatherDao.insertAirQualityCache(AirQualityCacheEntity("a1", "{}", 1000L))

        weatherDao.deleteAllCache()

        assertNull(weatherDao.getWeatherCache("w1"))
        assertNull(weatherDao.getAirQualityCache("a1"))
    }

    @Test
    fun deleteStaleCache_removesBothStaleWeatherAndAirQuality() = runTest {
        weatherDao.insertWeatherCache(WeatherCacheEntity("fresh", "{}", 2000L))
        weatherDao.insertWeatherCache(WeatherCacheEntity("stale", "{}", 500L))
        weatherDao.insertAirQualityCache(AirQualityCacheEntity("fresh", "{}", 2000L))
        weatherDao.insertAirQualityCache(AirQualityCacheEntity("stale", "{}", 500L))

        weatherDao.deleteStaleCache(1000L)

        assertNotNull(weatherDao.getWeatherCache("fresh"))
        assertNull(weatherDao.getWeatherCache("stale"))
        assertNotNull(weatherDao.getAirQualityCache("fresh"))
        assertNull(weatherDao.getAirQualityCache("stale"))
    }
}
