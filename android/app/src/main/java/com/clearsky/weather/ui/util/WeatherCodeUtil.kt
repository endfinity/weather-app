package com.clearsky.weather.ui.util

import androidx.compose.ui.graphics.Color

data class WeatherCondition(
    val description: String,
    val iconDay: String,
    val iconNight: String,
    val gradientDay: List<Color>,
    val gradientNight: List<Color>
)

object WeatherCodeUtil {

    private val conditions = mapOf(
        0 to WeatherCondition(
            "Clear sky", "clear_day", "clear_night",
            listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)),
            listOf(Color(0xFF0D1B2A), Color(0xFF1B2838))
        ),
        1 to WeatherCondition(
            "Mainly clear", "partly_cloudy_day", "partly_cloudy_night",
            listOf(Color(0xFF81D4FA), Color(0xFF039BE5)),
            listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))
        ),
        2 to WeatherCondition(
            "Partly cloudy", "partly_cloudy_day", "partly_cloudy_night",
            listOf(Color(0xFF90CAF9), Color(0xFF42A5F5)),
            listOf(Color(0xFF1A237E), Color(0xFF263238))
        ),
        3 to WeatherCondition(
            "Overcast", "overcast", "overcast",
            listOf(Color(0xFFB0BEC5), Color(0xFF78909C)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        45 to WeatherCondition(
            "Fog", "fog", "fog",
            listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)),
            listOf(Color(0xFF455A64), Color(0xFF37474F))
        ),
        48 to WeatherCondition(
            "Depositing rime fog", "fog", "fog",
            listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)),
            listOf(Color(0xFF455A64), Color(0xFF37474F))
        ),
        51 to WeatherCondition(
            "Light drizzle", "drizzle_light", "drizzle_light",
            listOf(Color(0xFF78909C), Color(0xFF546E7A)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        53 to WeatherCondition(
            "Moderate drizzle", "drizzle", "drizzle",
            listOf(Color(0xFF78909C), Color(0xFF546E7A)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        55 to WeatherCondition(
            "Dense drizzle", "drizzle", "drizzle",
            listOf(Color(0xFF607D8B), Color(0xFF455A64)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        56 to WeatherCondition(
            "Light freezing drizzle", "freezing_drizzle", "freezing_drizzle",
            listOf(Color(0xFF78909C), Color(0xFF546E7A)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        57 to WeatherCondition(
            "Dense freezing drizzle", "freezing_drizzle", "freezing_drizzle",
            listOf(Color(0xFF607D8B), Color(0xFF455A64)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        61 to WeatherCondition(
            "Slight rain", "rain_light", "rain_light",
            listOf(Color(0xFF546E7A), Color(0xFF37474F)),
            listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))
        ),
        63 to WeatherCondition(
            "Moderate rain", "rain", "rain",
            listOf(Color(0xFF455A64), Color(0xFF37474F)),
            listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))
        ),
        65 to WeatherCondition(
            "Heavy rain", "rain_heavy", "rain_heavy",
            listOf(Color(0xFF37474F), Color(0xFF263238)),
            listOf(Color(0xFF0D1B2A), Color(0xFF000A12))
        ),
        66 to WeatherCondition(
            "Light freezing rain", "freezing_rain", "freezing_rain",
            listOf(Color(0xFF546E7A), Color(0xFF37474F)),
            listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))
        ),
        67 to WeatherCondition(
            "Heavy freezing rain", "freezing_rain", "freezing_rain",
            listOf(Color(0xFF37474F), Color(0xFF263238)),
            listOf(Color(0xFF0D1B2A), Color(0xFF000A12))
        ),
        71 to WeatherCondition(
            "Slight snow fall", "snow_light", "snow_light",
            listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
            listOf(Color(0xFF546E7A), Color(0xFF455A64))
        ),
        73 to WeatherCondition(
            "Moderate snow fall", "snow", "snow",
            listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)),
            listOf(Color(0xFF455A64), Color(0xFF37474F))
        ),
        75 to WeatherCondition(
            "Heavy snow fall", "snow_heavy", "snow_heavy",
            listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        77 to WeatherCondition(
            "Snow grains", "snow_grains", "snow_grains",
            listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
            listOf(Color(0xFF546E7A), Color(0xFF455A64))
        ),
        80 to WeatherCondition(
            "Slight rain showers", "rain_showers_light", "rain_showers_light",
            listOf(Color(0xFF4DB6AC), Color(0xFF00897B)),
            listOf(Color(0xFF004D40), Color(0xFF1A237E))
        ),
        81 to WeatherCondition(
            "Moderate rain showers", "rain_showers", "rain_showers",
            listOf(Color(0xFF26A69A), Color(0xFF00796B)),
            listOf(Color(0xFF004D40), Color(0xFF1A237E))
        ),
        82 to WeatherCondition(
            "Violent rain showers", "rain_showers_heavy", "rain_showers_heavy",
            listOf(Color(0xFF00897B), Color(0xFF00695C)),
            listOf(Color(0xFF004D40), Color(0xFF0D1B2A))
        ),
        85 to WeatherCondition(
            "Slight snow showers", "snow_showers_light", "snow_showers_light",
            listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)),
            listOf(Color(0xFF455A64), Color(0xFF37474F))
        ),
        86 to WeatherCondition(
            "Heavy snow showers", "snow_showers_heavy", "snow_showers_heavy",
            listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE)),
            listOf(Color(0xFF37474F), Color(0xFF263238))
        ),
        95 to WeatherCondition(
            "Thunderstorm", "thunderstorm", "thunderstorm",
            listOf(Color(0xFF4A148C), Color(0xFF311B92)),
            listOf(Color(0xFF1A0033), Color(0xFF12005E))
        ),
        96 to WeatherCondition(
            "Thunderstorm with slight hail", "thunderstorm_hail", "thunderstorm_hail",
            listOf(Color(0xFF4A148C), Color(0xFF1A237E)),
            listOf(Color(0xFF12005E), Color(0xFF000A12))
        ),
        99 to WeatherCondition(
            "Thunderstorm with heavy hail", "thunderstorm_hail", "thunderstorm_hail",
            listOf(Color(0xFF311B92), Color(0xFF1A237E)),
            listOf(Color(0xFF12005E), Color(0xFF000A12))
        )
    )

    fun getCondition(weatherCode: Int): WeatherCondition =
        conditions[weatherCode] ?: conditions[0]!!

    fun getDescription(weatherCode: Int): String =
        getCondition(weatherCode).description

    fun getGradient(weatherCode: Int, isDay: Boolean): List<Color> {
        val condition = getCondition(weatherCode)
        return if (isDay) condition.gradientDay else condition.gradientNight
    }

    fun getIconName(weatherCode: Int, isDay: Boolean): String {
        val condition = getCondition(weatherCode)
        return if (isDay) condition.iconDay else condition.iconNight
    }

    fun getEmoji(weatherCode: Int): String = when (weatherCode) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55, 56, 57 -> "🌦️"
        61, 63, 65, 66, 67 -> "🌧️"
        71, 73, 75, 77 -> "🌨️"
        80, 81, 82 -> "🌧️"
        85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}
