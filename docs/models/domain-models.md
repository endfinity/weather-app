# Domain Models

Kotlin data classes for the domain layer. These are framework-agnostic and used throughout the app.

---

## Core Weather Models

```kotlin
// Location information
data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val timezoneAbbreviation: String,
    val utcOffsetSeconds: Int
)

// Current weather snapshot
data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val isDay: Boolean,
    val precipitation: Double,
    val rain: Double,
    val showers: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudCover: Int,
    val pressureMsl: Double,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double
)

// Single hour forecast data point
data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val precipitationProbability: Int,
    val precipitation: Double,
    val rain: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudCover: Int,
    val visibility: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double,
    val uvIndex: Double,
    val pressureMsl: Double,
    val dewPoint: Double,
    val isDay: Boolean
)

// Single day forecast data point
data class DailyForecast(
    val date: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val feelsLikeMax: Double,
    val feelsLikeMin: Double,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val rainSum: Double,
    val snowfallSum: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val sunrise: String,
    val sunset: String,
    val sunshineDuration: Long,     // seconds
    val daylightDuration: Long,     // seconds
    val uvIndexMax: Double,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val windDirectionDominant: Int,
    val precipitationHours: Int
)

// 15-minute precipitation data point
data class Minutely15(
    val time: String,
    val precipitation: Double,
    val rain: Double,
    val snowfall: Double,
    val weatherCode: Int
)

// Unit labels for display
data class WeatherUnits(
    val temperature: String,        // "°C" or "°F"
    val windSpeed: String,          // "km/h" or "mph"
    val precipitation: String,      // "mm" or "inch"
    val pressure: String,           // "hPa"
    val visibility: String          // "m"
)

// Complete weather data for a location
data class WeatherData(
    val location: WeatherLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val minutely15: List<Minutely15>?,  // null if not available for region
    val minutely15Available: Boolean,
    val units: WeatherUnits,
    val fetchedAt: Long                 // System.currentTimeMillis()
)
```

---

## Air Quality Models

```kotlin
// Current air quality snapshot
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

// Hourly AQ data point
data class HourlyAirQuality(
    val time: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val uvIndex: Double
)

// Pollen data (Europe only)
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

// Complete air quality data
data class AirQualityData(
    val current: CurrentAirQuality,
    val hourly: List<HourlyAirQuality>,
    val pollen: PollenData,
    val fetchedAt: Long
)

// AQI categories (US EPA standard)
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
```

---

## Location Models

```kotlin
// Geocoding search result
data class GeocodingResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val timezone: String,
    val country: String,
    val countryCode: String,
    val admin1: String?,
    val admin2: String?,
    val population: Int?,
    val featureCode: String?
)

// User-saved location
data class SavedLocation(
    val id: Long = 0,               // Room auto-generated
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val admin1: String?,             // State/province
    val timezone: String,
    val isCurrentLocation: Boolean,  // GPS-based
    val sortOrder: Int,
    val addedAt: Long
)
```

---

## Settings Models

```kotlin
// User preferences stored in DataStore
data class UserPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MM,
    val timeFormat: TimeFormat = TimeFormat.H12,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val severeAlertsEnabled: Boolean = true,
    val precipitationAlertsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val refreshIntervalMinutes: Int = 30,
    val defaultLocationId: Long? = null
)

enum class TemperatureUnit(val label: String, val symbol: String) {
    CELSIUS("Celsius", "°C"),
    FAHRENHEIT("Fahrenheit", "°F")
}

enum class WindSpeedUnit(val label: String, val symbol: String) {
    KMH("km/h", "km/h"),
    MPH("mph", "mph"),
    MS("m/s", "m/s"),
    KNOTS("Knots", "kn")
}

enum class PrecipitationUnit(val label: String, val symbol: String) {
    MM("Millimeters", "mm"),
    INCH("Inches", "in")
}

enum class TimeFormat(val label: String) {
    H12("12-hour"),
    H24("24-hour")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}
```

---

## Notification Models

```kotlin
// Device registration for push notifications
data class DeviceRegistration(
    val deviceId: String?,           // Assigned by server
    val fcmToken: String,
    val locations: List<SavedLocation>,
    val preferences: NotificationPreferences
)

data class NotificationPreferences(
    val severeAlerts: Boolean,
    val precipitationAlerts: Boolean,
    val dailySummary: Boolean,
    val alertThreshold: AlertThreshold
)

enum class AlertThreshold {
    LOW,        // All alerts
    MODERATE,   // Moderate and above
    HIGH        // Only severe/extreme
}

// Weather alert from backend
data class WeatherAlert(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val description: String,
    val locationName: String,
    val startTime: String,
    val endTime: String?,
    val issuedAt: String
)

enum class AlertType {
    THUNDERSTORM, EXTREME_TEMP, HIGH_WIND, HEAVY_PRECIPITATION,
    HIGH_UV, POOR_AIR_QUALITY, SNOWSTORM, FREEZING_RAIN
}

enum class AlertSeverity {
    MINOR, MODERATE, SEVERE, EXTREME
}
```

---

## UI State Models

```kotlin
// Main screen UI state
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val weather: WeatherData? = null,
    val airQuality: AirQualityData? = null,
    val currentLocation: SavedLocation? = null,
    val savedLocations: List<SavedLocation> = emptyList(),
    val selectedLocationIndex: Int = 0,
    val isOffline: Boolean = false,
    val lastUpdated: Long? = null,
    val activeAlerts: List<WeatherAlert> = emptyList()
)

// Search screen UI state
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<GeocodingResult> = emptyList(),
    val error: String? = null
)

// Settings screen UI state
data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isSaving: Boolean = false
)
```

---

## Weather Icon Mapping

The `weatherIcon` string in domain models maps to drawable resources:

| Icon Name | WMO Codes | Description |
|---|---|---|
| `clear_day` | 0 (day) | Clear sky |
| `clear_night` | 0 (night) | Clear sky |
| `partly_cloudy_day` | 1, 2 (day) | Mainly clear / Partly cloudy |
| `partly_cloudy_night` | 1, 2 (night) | Mainly clear / Partly cloudy |
| `overcast` | 3 | Overcast |
| `fog` | 45, 48 | Fog |
| `drizzle_light` | 51 | Light drizzle |
| `drizzle` | 53, 55 | Moderate/dense drizzle |
| `freezing_drizzle` | 56, 57 | Freezing drizzle |
| `rain_light` | 61 | Slight rain |
| `rain` | 63 | Moderate rain |
| `rain_heavy` | 65 | Heavy rain |
| `freezing_rain` | 66, 67 | Freezing rain |
| `snow_light` | 71 | Slight snowfall |
| `snow` | 73 | Moderate snowfall |
| `snow_heavy` | 75 | Heavy snowfall |
| `snow_grains` | 77 | Snow grains |
| `rain_showers_light` | 80 | Slight rain showers |
| `rain_showers` | 81 | Moderate rain showers |
| `rain_showers_heavy` | 82 | Violent rain showers |
| `snow_showers_light` | 85 | Slight snow showers |
| `snow_showers_heavy` | 86 | Heavy snow showers |
| `thunderstorm` | 95 | Thunderstorm |
| `thunderstorm_hail` | 96, 99 | Thunderstorm with hail |
