# WMO Weather Interpretation Codes (WW)

Reference for mapping Open-Meteo `weather_code` values to descriptions and icon names.

## Code Table

| Code | Description | Icon (Day) | Icon (Night) | Severity |
|------|-------------|------------|--------------|----------|
| 0 | Clear sky | `clear_day` | `clear_night` | None |
| 1 | Mainly clear | `partly_cloudy_day` | `partly_cloudy_night` | None |
| 2 | Partly cloudy | `partly_cloudy_day` | `partly_cloudy_night` | None |
| 3 | Overcast | `overcast` | `overcast` | None |
| 45 | Fog | `fog` | `fog` | Low |
| 48 | Depositing rime fog | `fog` | `fog` | Low |
| 51 | Drizzle: Light | `drizzle_light` | `drizzle_light` | Low |
| 53 | Drizzle: Moderate | `drizzle` | `drizzle` | Low |
| 55 | Drizzle: Dense | `drizzle` | `drizzle` | Low |
| 56 | Freezing drizzle: Light | `freezing_drizzle` | `freezing_drizzle` | Moderate |
| 57 | Freezing drizzle: Dense | `freezing_drizzle` | `freezing_drizzle` | Moderate |
| 61 | Rain: Slight | `rain_light` | `rain_light` | Low |
| 63 | Rain: Moderate | `rain` | `rain` | Low |
| 65 | Rain: Heavy | `rain_heavy` | `rain_heavy` | Moderate |
| 66 | Freezing rain: Light | `freezing_rain` | `freezing_rain` | Moderate |
| 67 | Freezing rain: Heavy | `freezing_rain` | `freezing_rain` | High |
| 71 | Snow fall: Slight | `snow_light` | `snow_light` | Low |
| 73 | Snow fall: Moderate | `snow` | `snow` | Moderate |
| 75 | Snow fall: Heavy | `snow_heavy` | `snow_heavy` | High |
| 77 | Snow grains | `snow_grains` | `snow_grains` | Low |
| 80 | Rain showers: Slight | `rain_showers_light` | `rain_showers_light` | Low |
| 81 | Rain showers: Moderate | `rain_showers` | `rain_showers` | Moderate |
| 82 | Rain showers: Violent | `rain_showers_heavy` | `rain_showers_heavy` | High |
| 85 | Snow showers: Slight | `snow_showers_light` | `snow_showers_light` | Moderate |
| 86 | Snow showers: Heavy | `snow_showers_heavy` | `snow_showers_heavy` | High |
| 95 | Thunderstorm: Slight or moderate | `thunderstorm` | `thunderstorm` | High |
| 96 | Thunderstorm with slight hail | `thunderstorm_hail` | `thunderstorm_hail` | Severe |
| 99 | Thunderstorm with heavy hail | `thunderstorm_hail` | `thunderstorm_hail` | Severe |

> **Note**: Thunderstorm forecast with hail (codes 96, 99) is only available in Central Europe.

## Backend Alert Trigger Conditions

Weather codes that trigger push notification consideration:

| Condition | Trigger | Alert Severity |
|---|---|---|
| Thunderstorm | Code ≥ 95 | Severe |
| Heavy rain / violent showers | Code 65 or 82 | Moderate |
| Heavy snowfall | Code 75 or 86 | Moderate |
| Freezing rain (heavy) | Code 67 | High |
| Extreme heat | Temp > 40°C (104°F) | Severe |
| Extreme cold | Temp < -20°C (-4°F) | Severe |
| High winds | Gusts > 90 km/h (56 mph) | High |
| Heavy precipitation rate | > 20 mm/hr | Moderate |
| Very high UV | UV Index ≥ 8 | Moderate |
| Unhealthy air | AQI ≥ 151 | Moderate |

## Gradient Background Mapping

For the app's dynamic background:

| Codes | Condition Group | Gradient Theme |
|---|---|---|
| 0 | Clear | Sky blue (day) / Deep navy (night) |
| 1, 2 | Partly cloudy | Soft blue (day) / Dark blue-gray (night) |
| 3 | Overcast | Gray (day) / Charcoal (night) |
| 45, 48 | Fog | Soft gray (day) / Dark gray (night) |
| 51–57 | Drizzle | Blue-gray |
| 61–67 | Rain | Dark blue-gray (day) / Deep navy (night) |
| 71–77 | Snow | Cool white-blue (day) / Steel gray (night) |
| 80–86 | Showers | Blue-teal |
| 95–99 | Thunderstorm | Dark purple (day) / Near-black purple (night) |
