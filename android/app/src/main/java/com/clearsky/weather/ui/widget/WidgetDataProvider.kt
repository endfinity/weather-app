package com.clearsky.weather.ui.widget

import android.content.Context
import androidx.room.Room
import com.clearsky.weather.BuildConfig
import com.clearsky.weather.data.local.ClearSkyDatabase
import com.clearsky.weather.data.mapper.*
import com.clearsky.weather.data.remote.ClearSkyApi
import com.clearsky.weather.data.remote.dto.AirQualityResponseDto
import com.clearsky.weather.data.remote.dto.WeatherResponseDto
import com.clearsky.weather.domain.model.DailyForecast
import com.clearsky.weather.domain.model.HourlyForecast
import com.clearsky.weather.domain.model.WeatherData
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Lightweight data class for widget display  only what widgets need.
 */
data class WidgetWeatherData(
    val locationName: String,
    val temperature: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Double,
    val highTemp: Double,
    val lowTemp: Double,
    val hourlyForecasts: List<WidgetHourlyItem>,
    val dailyForecasts: List<WidgetDailyItem>,
    val precipitation: Double,
    val uvIndex: Double,
    val airQualityIndex: Int?,
    val fetchedAt: Long
) {
    val isStale: Boolean
        get() = System.currentTimeMillis() - fetchedAt > 60 * 60 * 1000L // 1 hour

    val ageMinutes: Long
        get() = (System.currentTimeMillis() - fetchedAt) / (60 * 1000L)
}

data class WidgetHourlyItem(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val precipitationProbability: Int
)

data class WidgetDailyItem(
    val date: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val weatherCode: Int,
    val precipitationProbabilityMax: Int
)

/**
 * Provides weather data for widgets without Hilt DI.
 * Widgets run in a separate process context, so we create
 * lightweight API + DB instances directly.
 */
object WidgetDataProvider {

    private var api: ClearSkyApi? = null
    private var database: ClearSkyDatabase? = null

    private fun getApi(): ClearSkyApi {
        if (api == null) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val contentType = "application/json".toMediaType()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
            api = retrofit.create(ClearSkyApi::class.java)
        }
        return api!!
    }

    private fun getDatabase(context: Context): ClearSkyDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                ClearSkyDatabase::class.java,
                "clearsky_database"
            )
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
        }
        return database!!
    }

    /**
     * Fetches weather data for the first saved location.
     * Falls back to cached data from Room when network fails.
     * Returns null if no locations saved and no cache available.
     */
    suspend fun getWidgetData(context: Context): WidgetWeatherData? {
        return try {
            val db = getDatabase(context)
            val locations = db.locationDao().getAllLocationsList()

            if (locations.isEmpty()) return null

            val location = locations.first()

            try {
                val response = getApi().getCombinedWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                val weatherData = response.data.weather.toDomain()
                val airQuality = try {
                    response.data.airQuality.toDomain()
                } catch (_: Exception) {
                    null
                }

                mapToWidgetData(
                    locationName = location.name,
                    weather = weatherData,
                    aqiIndex = airQuality?.current?.aqi,
                    fetchedAt = System.currentTimeMillis()
                )
            } catch (_: Exception) {
                getCachedWidgetData(db, location.latitude, location.longitude, location.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getCachedWidgetData(
        db: ClearSkyDatabase,
        latitude: Double,
        longitude: Double,
        locationName: String
    ): WidgetWeatherData? {
        return try {
            val cacheKey = "${String.format("%.4f", latitude)}_${String.format("%.4f", longitude)}"
            val cached = db.weatherDao().getWeatherCache(cacheKey) ?: return null

            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }
            val weatherDto = json.decodeFromString<WeatherResponseDto>(cached.weatherJson)
            val weatherData = weatherDto.toDomain().copy(fetchedAt = cached.fetchedAt)

            val aqiIndex = try {
                val aqiCached = db.weatherDao().getAirQualityCache(cacheKey)
                if (aqiCached != null) {
                    val aqiDto = json.decodeFromString<AirQualityResponseDto>(aqiCached.airQualityJson)
                    aqiDto.toDomain().current.aqi
                } else null
            } catch (_: Exception) {
                null
            }

            mapToWidgetData(
                locationName = locationName,
                weather = weatherData,
                aqiIndex = aqiIndex,
                fetchedAt = cached.fetchedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun mapToWidgetData(
        locationName: String,
        weather: WeatherData,
        aqiIndex: Int?,
        fetchedAt: Long
    ): WidgetWeatherData {
        val today = weather.daily.firstOrNull()
        val currentHourIndex = weather.hourly.indexOfFirst { hourly ->
            hourly.time >= weather.current.time
        }.coerceAtLeast(0)

        return WidgetWeatherData(
            locationName = locationName,
            temperature = weather.current.temperature,
            feelsLike = weather.current.feelsLike,
            weatherCode = weather.current.weatherCode,
            weatherDescription = weather.current.weatherDescription,
            isDay = weather.current.isDay,
            humidity = weather.current.humidity,
            windSpeed = weather.current.windSpeed,
            highTemp = today?.temperatureMax ?: weather.current.temperature,
            lowTemp = today?.temperatureMin ?: weather.current.temperature,
            hourlyForecasts = weather.hourly
                .drop(currentHourIndex)
                .take(8)
                .map { it.toWidgetHourly() },
            dailyForecasts = weather.daily
                .take(7)
                .map { it.toWidgetDaily() },
            precipitation = weather.current.precipitation,
            uvIndex = weather.hourly.getOrNull(currentHourIndex)?.uvIndex ?: 0.0,
            airQualityIndex = aqiIndex,
            fetchedAt = fetchedAt
        )
    }

    private fun HourlyForecast.toWidgetHourly() = WidgetHourlyItem(
        time = time,
        temperature = temperature,
        weatherCode = weatherCode,
        isDay = isDay,
        precipitationProbability = precipitationProbability
    )

    private fun DailyForecast.toWidgetDaily() = WidgetDailyItem(
        date = date,
        temperatureMax = temperatureMax,
        temperatureMin = temperatureMin,
        weatherCode = weatherCode,
        precipitationProbabilityMax = precipitationProbabilityMax
    )
}
