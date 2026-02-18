package com.clearsky.weather.domain.model

data class CurrentAirQuality(
    val time: String,
    val aqi: Int,
    val aqiCategory: AqiCategory,
    val pm25: Double,
    val pm10: Double,
    val carbonMonoxide: Double,
    val nitrogenDioxide: Double,
    val sulphurDioxide: Double,
    val ozone: Double,
    val uvIndex: Double,
    val uvIndexClearSky: Double
)

data class HourlyAirQuality(
    val time: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val uvIndex: Double
)

data class PollenData(
    val available: Boolean,
    val hourly: List<HourlyPollen>?
)

data class HourlyPollen(
    val time: String,
    val alderPollen: Double,
    val birchPollen: Double,
    val grassPollen: Double,
    val mugwortPollen: Double,
    val olivePollen: Double,
    val ragweedPollen: Double
)

data class AirQualityData(
    val current: CurrentAirQuality,
    val hourly: List<HourlyAirQuality>,
    val pollen: PollenData,
    val fetchedAt: Long
)

enum class AqiCategory(val label: String, val range: IntRange) {
    GOOD("Good", 0..50),
    MODERATE("Moderate", 51..100),
    UNHEALTHY_SENSITIVE("Unhealthy for Sensitive Groups", 101..150),
    UNHEALTHY("Unhealthy", 151..200),
    VERY_UNHEALTHY("Very Unhealthy", 201..300),
    HAZARDOUS("Hazardous", 301..500);

    companion object {
        fun fromAqi(aqi: Int): AqiCategory =
            entries.first { aqi in it.range }
    }
}
