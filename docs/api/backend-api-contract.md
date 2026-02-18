# Backend API Contract

Base URL: `http://localhost:3000/api` (dev) / `https://your-server.com/api` (prod)

All responses follow the envelope format:
```json
{
  "success": true,
  "data": { ... },
  "cached": true,
  "cachedAt": "2026-02-07T12:00:00Z"
}
```

Error responses:
```json
{
  "success": false,
  "error": {
    "code": "INVALID_COORDINATES",
    "message": "Latitude must be between -90 and 90"
  }
}
```

---

## 1. GET `/api/weather`

Fetches combined current conditions, hourly forecast, daily forecast, and 15-minutely precipitation for a location.

### Query Parameters

| Param | Type | Required | Default | Description |
|---|---|---|---|---|
| `lat` | float | Yes | — | Latitude (-90 to 90) |
| `lon` | float | Yes | — | Longitude (-180 to 180) |
| `units` | string | No | `metric` | `metric` (°C, km/h, mm) or `imperial` (°F, mph, inch) |
| `timezone` | string | No | `auto` | IANA timezone or `auto` |

### Response: `data` object

```json
{
  "location": {
    "latitude": 40.7128,
    "longitude": -74.006,
    "elevation": 10.0,
    "timezone": "America/New_York",
    "timezoneAbbreviation": "EST",
    "utcOffsetSeconds": -18000
  },
  "current": {
    "time": "2026-02-07T14:00",
    "temperature": 3.2,
    "feelsLike": -1.5,
    "humidity": 65,
    "isDay": true,
    "precipitation": 0.0,
    "rain": 0.0,
    "showers": 0.0,
    "snowfall": 0.0,
    "weatherCode": 2,
    "weatherDescription": "Partly cloudy",
    "weatherIcon": "partly_cloudy_day",
    "cloudCover": 45,
    "pressureMsl": 1018.5,
    "surfacePressure": 1017.2,
    "windSpeed": 12.5,
    "windDirection": 225,
    "windGusts": 22.0
  },
  "hourly": {
    "time": ["2026-02-07T00:00", "2026-02-07T01:00", "..."],
    "temperature": [1.2, 0.8, "..."],
    "feelsLike": [-2.1, -2.8, "..."],
    "humidity": [70, 72, "..."],
    "precipitationProbability": [10, 15, "..."],
    "precipitation": [0.0, 0.0, "..."],
    "rain": [0.0, 0.0, "..."],
    "snowfall": [0.0, 0.0, "..."],
    "weatherCode": [1, 2, "..."],
    "weatherDescription": ["Mainly clear", "Partly cloudy", "..."],
    "weatherIcon": ["clear_night", "partly_cloudy_night", "..."],
    "cloudCover": [20, 45, "..."],
    "visibility": [10000, 10000, "..."],
    "windSpeed": [8.5, 9.2, "..."],
    "windDirection": [220, 225, "..."],
    "windGusts": [15.0, 16.5, "..."],
    "uvIndex": [0, 0, "..."],
    "pressureMsl": [1018.5, 1018.3, "..."],
    "dewPoint": [-3.2, -3.5, "..."],
    "isDay": [false, false, "..."]
  },
  "daily": {
    "time": ["2026-02-07", "2026-02-08", "... (10 days)"],
    "temperatureMax": [5.2, 7.1, "..."],
    "temperatureMin": [-1.3, 0.5, "..."],
    "feelsLikeMax": [2.1, 4.5, "..."],
    "feelsLikeMin": [-5.2, -3.1, "..."],
    "precipitationSum": [0.0, 2.5, "..."],
    "precipitationProbabilityMax": [10, 65, "..."],
    "rainSum": [0.0, 2.5, "..."],
    "snowfallSum": [0.0, 0.0, "..."],
    "weatherCode": [2, 61, "..."],
    "weatherDescription": ["Partly cloudy", "Slight rain", "..."],
    "weatherIcon": ["partly_cloudy_day", "rain_light", "..."],
    "sunrise": ["2026-02-07T07:02", "2026-02-08T07:01", "..."],
    "sunset": ["2026-02-07T17:22", "2026-02-08T17:23", "..."],
    "sunshineDuration": [28800, 18000, "..."],
    "daylightDuration": [37200, 37260, "..."],
    "uvIndexMax": [3, 2, "..."],
    "windSpeedMax": [22.5, 30.0, "..."],
    "windGustsMax": [35.0, 45.0, "..."],
    "windDirectionDominant": [225, 270, "..."],
    "precipitationHours": [0, 4, "..."]
  },
  "minutely15": {
    "available": true,
    "time": ["2026-02-07T14:00", "2026-02-07T14:15", "... (next 2 hours)"],
    "precipitation": [0.0, 0.0, "..."],
    "rain": [0.0, 0.0, "..."],
    "snowfall": [0.0, 0.0, "..."],
    "weatherCode": [2, 2, "..."]
  },
  "units": {
    "temperature": "°C",
    "windSpeed": "km/h",
    "precipitation": "mm",
    "pressure": "hPa",
    "visibility": "m"
  }
}
```

---

## 2. GET `/api/air-quality`

Fetches air quality data for a location.

### Query Parameters

| Param | Type | Required | Default | Description |
|---|---|---|---|---|
| `lat` | float | Yes | — | Latitude |
| `lon` | float | Yes | — | Longitude |

### Response: `data` object

```json
{
  "current": {
    "time": "2026-02-07T14:00",
    "aqi": 42,
    "aqiCategory": "Good",
    "pm25": 8.5,
    "pm10": 15.2,
    "carbonMonoxide": 220.0,
    "nitrogenDioxide": 12.5,
    "sulphurDioxide": 3.2,
    "ozone": 65.0,
    "uvIndex": 3,
    "uvIndexClearSky": 4
  },
  "hourly": {
    "time": ["2026-02-07T00:00", "2026-02-07T01:00", "..."],
    "aqi": [38, 40, "..."],
    "pm25": [7.2, 8.0, "..."],
    "pm10": [14.0, 14.5, "..."],
    "ozone": [60.0, 62.0, "..."],
    "uvIndex": [0, 0, "..."]
  },
  "pollen": {
    "available": false,
    "note": "Pollen data only available in Europe"
  }
}
```

When pollen data IS available (Europe):
```json
{
  "pollen": {
    "available": true,
    "hourly": {
      "time": ["..."],
      "alderPollen": [0, 0, "..."],
      "birchPollen": [5, 8, "..."],
      "grassPollen": [0, 0, "..."],
      "mugwortPollen": [0, 0, "..."],
      "olivePollen": [0, 0, "..."],
      "ragweedPollen": [0, 0, "..."]
    }
  }
}
```

---

## 3. GET `/api/geocoding/search`

Searches for cities/locations by name.

### Query Parameters

| Param | Type | Required | Default | Description |
|---|---|---|---|---|
| `query` | string | Yes | — | Search term (city name or postal code, min 2 chars) |
| `count` | int | No | `10` | Number of results (1–100) |
| `lang` | string | No | `en` | Language for translated results |

### Response: `data` object

```json
{
  "results": [
    {
      "id": 5128581,
      "name": "New York",
      "latitude": 40.71427,
      "longitude": -74.00597,
      "elevation": 10.0,
      "timezone": "America/New_York",
      "country": "United States",
      "countryCode": "US",
      "admin1": "New York",
      "admin2": "New York",
      "population": 8804190,
      "featureCode": "PPL"
    }
  ]
}
```

---

## 4. POST `/api/devices/register`

Registers a device for push notifications.

### Request Body

```json
{
  "fcmToken": "device-fcm-token-string",
  "locations": [
    {
      "latitude": 40.7128,
      "longitude": -74.006,
      "name": "New York"
    }
  ],
  "preferences": {
    "severeAlerts": true,
    "precipitationAlerts": true,
    "dailySummary": false,
    "alertThreshold": "moderate"
  }
}
```

### Response

```json
{
  "success": true,
  "data": {
    "deviceId": "uuid-string",
    "registeredAt": "2026-02-07T14:00:00Z"
  }
}
```

---

## 5. PUT `/api/devices/:deviceId`

Updates device locations or notification preferences.

### Request Body

Same structure as POST registration body.

---

## 6. DELETE `/api/devices/:deviceId`

Unregisters a device from push notifications.

---

## 7. GET `/api/weather/combined`

**Convenience endpoint** — fetches weather + air quality in a single call.

### Query Parameters

Same as `/api/weather` plus all `/api/air-quality` params.

### Response

Merges weather and air quality responses into a single `data` object:
```json
{
  "weather": { "... (same as /api/weather response)" },
  "airQuality": { "... (same as /api/air-quality response)" }
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| `INVALID_COORDINATES` | 400 | Lat/lon out of valid range |
| `MISSING_PARAMETER` | 400 | Required query parameter missing |
| `INVALID_QUERY` | 400 | Search query too short (< 2 chars) |
| `UPSTREAM_ERROR` | 502 | Open-Meteo API returned an error |
| `UPSTREAM_TIMEOUT` | 504 | Open-Meteo API did not respond in time |
| `RATE_LIMITED` | 429 | Too many requests from this client |
| `DEVICE_NOT_FOUND` | 404 | Device ID not found for update/delete |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## Rate Limits

| Tier | Limit |
|---|---|
| Per IP | 60 requests / minute |
| Backend → Open-Meteo | < 10,000 calls / day (free tier) |

---

## Open-Meteo API Variables Reference

### Forecast API request variables

**Hourly**: `temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,weather_code,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,pressure_msl,is_day`

**Daily**: `temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,precipitation_sum,precipitation_probability_max,rain_sum,snowfall_sum,weather_code,sunrise,sunset,sunshine_duration,daylight_duration,uv_index_max,wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant,precipitation_hours`

**Current**: `temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,snowfall,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m`

**15-Minutely**: `precipitation,rain,snowfall,weather_code`

**Additional params**: `&forecast_days=10&timezone=auto&minutely_15=precipitation,rain,snowfall,weather_code`

### Air Quality API request variables

**Hourly**: `pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,us_aqi,uv_index,uv_index_clear_sky,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen`

**Current**: `us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,uv_index,uv_index_clear_sky`
