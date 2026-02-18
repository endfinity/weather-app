<div align="center">

# ☀️ ClearSky Weather

**A feature-rich native Android weather app powered by Open-Meteo — the most accurate free weather data available.**

Built with Kotlin, Jetpack Compose, Material 3, and backed by a Node.js proxy server.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2026.01.01-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Node.js](https://img.shields.io/badge/Node.js-22_LTS-339933?logo=nodedotjs&logoColor=white)](https://nodejs.org)
[![API](https://img.shields.io/badge/Data-Open--Meteo-1E88E5)](https://open-meteo.com)
[![License](https://img.shields.io/badge/License-Personal_Use-blue)](#license)

</div>

---

## Why Open-Meteo?

Open-Meteo aggregates the best weather models from national weather services worldwide (NOAA HRRR, DWD ICON, ECMWF IFS, Météo-France AROME, UK Met Office, and more). For any given location, it automatically selects the highest-resolution model available — delivering accuracy that rivals or exceeds paid alternatives, completely free for non-commercial use.

No API key required. No usage limits for personal projects.

---

## Features

### 🌤️ Weather Data
- **Current conditions** — temperature, feels-like, weather description, animated icons
- **Hourly forecast** — 24-hour scrollable strip with precipitation probability
- **10-day daily forecast** — high/low temps, weather codes, precipitation bars
- **15-minute precipitation nowcast** — rain intensity bars for the next few hours
- **Air quality index** — AQI, PM2.5, PM10, O₃, NO₂ with health advice
- **UV index** — scale bar, level label, protection advice
- **Wind** — speed, gusts, direction with animated compass rose
- **Humidity** — percentage gauge, dew point
- **Pressure** — MSL + surface pressure, trend (low/normal/high)
- **Sunrise & sunset** — sun arc canvas showing daylight progress
- **Visibility** — distance with condition description
- **Feels like** — temperature with wind-chill/humidity explanation
- **Weather alerts** — severity-colored banners from Open-Meteo

### 📍 Locations
- **GPS detection** — auto-detect current location with permission handling
- **City search** — autocomplete via Open-Meteo Geocoding API
- **Multiple locations** — swipeable horizontal pager with page indicators
- **Location management** — add, reorder, delete saved locations

### 🏠 Home Screen Widgets
- **Small widget** — current temperature and weather icon
- **Medium widget** — temperature, location, high/low, condition
- **Large widget** — current conditions + daily forecast rows
- **XL widget** — full weather dashboard with hourly strip
- **Compact forecast** — minimal daily forecast widget
- **AQI widget** — air quality index with color-coded scale
- **Auto-refresh** — WorkManager periodic updates every 1–3 hours

### ⚙️ Preferences
- **Temperature unit** — Celsius / Fahrenheit
- **Wind speed unit** — km/h / mph / m/s / knots
- **Precipitation unit** — mm / inches
- **Time format** — 12h / 24h
- **Theme** — Light / Dark / System
- **Dynamic Color** — Material You wallpaper-based theming (Android 12+)

### 🌍 Localization
- **English** (default)
- **Spanish** (Español)
- **Malay** (Bahasa Melayu)
- All 155+ string resources externalized for easy translation

### ✨ Polish
- **Shimmer loading skeleton** — card-shaped placeholders while data loads
- **Staggered card animations** — weather cards fade + slide in sequentially
- **Animated temperature counter** — smooth number transitions on data change
- **Slide + fade navigation** — screen transitions in NavGraph
- **Pull-to-refresh** — swipe down to refresh weather data
- **Offline mode** — cached data with offline banner indicator
- **Accessibility** — content descriptions on all interactive elements

---

## Architecture

```
┌─────────────────────────┐     ┌────────────────────────┐     ┌─────────────────────┐
│      Android App        │────▶│     Backend Server      │────▶│   Open-Meteo APIs   │
│    (Kotlin / Compose)   │◀────│   (Node.js / Express)   │◀────│   (Free, no key)    │
├─────────────────────────┤     ├────────────────────────┤     ├─────────────────────┤
│ Jetpack Compose (M3)    │     │ Proxy & aggregation    │     │ Forecast API        │
│ MVVM + Clean Arch       │     │ In-memory caching      │     │ Air Quality API     │
│ Hilt dependency inject. │     │ Rate limiting          │     │ Geocoding API       │
│ Room DB (offline cache) │     │ ETag / conditional req │     │ Historical API      │
│ DataStore (preferences) │     │ Alert monitoring       │     │                     │
│ Glance (widgets)        │     │ FCM push notifications │     │                     │
│ WorkManager (bg sync)   │     │ Swagger docs           │     │                     │
│ Navigation Compose      │     │ Zod input validation   │     │                     │
└─────────────────────────┘     └────────────────────────┘     └─────────────────────┘
```

### Android — Clean Architecture layers

```
com.clearsky.weather/
├── data/                  # Data layer
│   ├── local/             # Room DB, DAOs, DataStore preferences
│   ├── remote/            # Retrofit API interfaces
│   ├── repository/        # Repository implementations
│   ├── mapper/            # Entity ↔ Domain model mappers
│   ├── location/          # FusedLocationProvider wrapper
│   └── notification/      # FCM token manager, notification service
├── domain/                # Domain layer (pure Kotlin)
│   ├── model/             # Domain models (Weather, Location, Settings…)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Use cases
├── di/                    # Hilt modules (Database, Network, Repository, Location)
└── ui/                    # Presentation layer
    ├── home/              # HomeScreen + HomeViewModel + 16 weather card components
    ├── search/            # SearchScreen + SearchViewModel
    ├── settings/          # SettingsScreen + SettingsViewModel
    ├── locations/         # LocationManagementScreen
    ├── alerts/            # Alert detail screens
    ├── widget/            # 6 Glance widgets + update worker
    ├── common/            # Shared components (ShimmerEffect, PagerIndicator…)
    ├── theme/             # ClearSkyTheme, color schemes, typography
    ├── navigation/        # NavGraph with type-safe routes
    └── util/              # FormatUtil, UnitConverter, WeatherCodeUtil, WeatherIcon
```

### Backend — Express.js service layer

```
backend/src/
├── app.js                 # Express app setup (middleware, routes, error handling)
├── index.js               # Server entry point + graceful shutdown
├── config.js              # Environment config with Zod validation
├── routes/                # REST endpoints
│   ├── weather.js         #   /api/weather — forecast data
│   ├── airQuality.js      #   /api/air-quality — AQI + pollutants
│   ├── geocoding.js       #   /api/geocoding — city search
│   ├── historical.js      #   /api/historical — past weather
│   ├── alerts.js          #   /api/alerts — weather alert subscriptions
│   ├── devices.js         #   /api/devices — FCM device registration
│   ├── radar.js           #   /api/radar — radar tile URLs
│   ├── premium.js         #   /api/premium — premium feature stubs
│   ├── apiKeys.js         #   /api/keys — API key management
│   └── v2/               #   API versioning (v2 endpoints)
├── services/              # Business logic
│   ├── openMeteoService   # Open-Meteo API client
│   ├── transformService   # Response normalization
│   ├── cacheService       # In-memory TTL caching
│   ├── alertService       # Alert detection + storage
│   ├── alertScheduler     # Background alert monitoring
│   ├── notificationService# FCM push notification dispatch
│   ├── firebaseService    # Firebase Admin SDK wrapper
│   └── apiKeyService      # API key CRUD
├── middleware/             # Express middleware (rate limit, validation, auth…)
├── db/                    # SQLite schema + migrations
├── jobs/                  # Scheduled background tasks
└── swagger.js             # OpenAPI 3.0 spec + Swagger UI
```

---

## Tech Stack

### Android App

| Component | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI Framework | Jetpack Compose (BOM 2026.01.01) + Material 3 |
| Widgets | Jetpack Glance |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp + Kotlin Serialization |
| Local Database | Room |
| Preferences | DataStore |
| Navigation | Compose Navigation (type-safe routes) |
| Location | FusedLocationProviderClient (Google Play Services) |
| Background | WorkManager |
| Notifications | Firebase Cloud Messaging |
| Compile SDK | 36 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

### Backend Server

| Component | Technology |
|---|---|
| Runtime | Node.js 22 LTS |
| Framework | Express 5 (ES modules) |
| Validation | Zod 4 |
| Caching | node-cache (in-memory TTL) |
| Database | SQLite via better-sqlite3 |
| Push Notifications | Firebase Admin SDK |
| API Docs | Swagger UI + swagger-jsdoc (OpenAPI 3.0) |
| Logging | Pino |
| Security | Helmet, CORS, rate limiting |
| Linting | ESLint 10 + Prettier |
| Testing | Node.js built-in test runner |
| Containerization | Docker (multi-stage) + docker-compose |

### Data Source — Open-Meteo (free, no API key)

| API | Endpoint | Purpose |
|---|---|---|
| Weather Forecast | `api.open-meteo.com/v1/forecast` | Current + hourly + daily + 15-min |
| Air Quality | `air-quality-api.open-meteo.com/v1/air-quality` | AQI, PM2.5, PM10, O₃, NO₂ |
| Geocoding | `geocoding-api.open-meteo.com/v1/search` | City name → coordinates |
| Historical | `archive-api.open-meteo.com/v1/archive` | Past weather data |

---

## Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.3+) or newer
- **JDK 17+**
- **Node.js 22 LTS** (for the backend)
- **Docker** (optional, for containerized backend)

### 1. Clone the repository

```bash
git clone https://github.com/your-username/weather-app.git
cd weather-app
```

### 2. Backend Setup

```bash
cd backend

# Copy environment template
cp .env.example .env

# Install dependencies
npm install

# Start the dev server (port 3000)
npm run dev
```

The backend requires no API keys — Open-Meteo is free and keyless.

**Environment variables** (all have sensible defaults in `.env.example`):

| Variable | Default | Description |
|---|---|---|
| `PORT` | `3000` | Server port |
| `NODE_ENV` | `development` | Environment mode |
| `CACHE_TTL_SECONDS` | `900` | Weather cache duration (15 min) |
| `RATE_LIMIT_MAX_REQUESTS` | `60` | Requests per minute per IP |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | — | Firebase credentials (optional, for push) |

Once running, access the API docs at **http://localhost:3000/api-docs**.

### 3. Android App Setup

1. Open the `android/` folder in Android Studio
2. Sync Gradle — dependencies will download automatically
3. The debug build points to `http://10.0.2.2:3000/api/` (emulator → host)
4. Run on an emulator or physical device (min SDK 26)

> **Physical device?** Update `BASE_URL` in `build.gradle.kts` to your machine's local IP.

### 4. Docker (production backend)

```bash
# From the project root
docker compose up -d

# Check logs
docker compose logs -f backend

# The API is available at http://localhost:3000
```

---

## Running Tests

### Backend

```bash
cd backend

# Run all tests (unit + integration)
npm test

# Lint
npm run lint

# Format
npm run format:check
```

**Test coverage:**
- **9 unit tests** — cache, transform, rate limiter, envelope, env schema, conditional requests, weather codes, alert scheduler, units
- **14 integration tests** — weather, air quality, geocoding, historical, health, batch, ETag, API keys, alerts, premium, radar, versioning, timeout, combined

### Android

Open in Android Studio and run tests via the test runner, or:

```bash
cd android
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # Instrumentation tests
```

---

## Project Structure

```
weather-app/
├── android/                   # Android application
│   └── app/
│       ├── build.gradle.kts   # App-level Gradle config
│       └── src/main/
│           ├── java/com/clearsky/weather/
│           │   ├── data/      # Repositories, Room DB, Retrofit, DataStore
│           │   ├── domain/    # Models, use cases, repository interfaces
│           │   ├── di/        # Hilt DI modules
│           │   └── ui/        # Compose screens, ViewModels, widgets
│           └── res/
│               ├── values/         # English strings (155+ entries)
│               ├── values-es/      # Spanish translation
│               ├── values-ms/      # Malay translation
│               ├── values-night/   # Dark theme overrides
│               ├── drawable/       # Icons and vector assets
│               └── xml/            # Widget metadata, backup rules
├── backend/                   # Node.js backend server
│   ├── src/                   # Application source
│   ├── tests/                 # Unit + integration tests
│   ├── Dockerfile             # Multi-stage production build
│   ├── package.json           # Dependencies and scripts
│   └── .env.example           # Environment variable template
├── docs/                      # Project documentation
│   ├── architecture.md        # System architecture deep-dive
│   ├── milestones.md          # Development roadmap
│   ├── api/                   # Backend API contract
│   ├── design/                # Screen specs and UI layouts
│   ├── models/                # Domain model definitions
│   └── wmo-weather-codes.md   # Weather code reference
├── store-listing/             # Google Play assets
│   ├── listing.md             # Store listing copy
│   └── release-notes.md       # Release notes
├── docker-compose.yml         # Container orchestration
└── README.md                  # ← You are here
```

---

## API Endpoints

The backend exposes a REST API documented with Swagger. Key endpoints:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/weather` | Forecast (current + hourly + daily + minutely) |
| `GET` | `/api/air-quality` | AQI and pollutant concentrations |
| `GET` | `/api/geocoding/search` | City search autocomplete |
| `GET` | `/api/historical` | Historical weather data |
| `GET` | `/api/alerts` | Active weather alerts for a location |
| `POST` | `/api/alerts/subscribe` | Subscribe to alert notifications |
| `POST` | `/api/devices/register` | Register FCM device token |
| `GET` | `/api/radar/tiles` | Radar tile layer URLs |
| `GET` | `/api/health` | Health check |
| `GET` | `/api-docs` | Swagger UI |

All responses use a standardized envelope:
```json
{
  "status": "success",
  "data": { ... },
  "meta": { "cached": true, "timestamp": "..." }
}
```

---

## Documentation

| Document | Description |
|---|---|
| [Architecture](docs/architecture.md) | System architecture, tech stack, design decisions |
| [API Contract](docs/api/backend-api-contract.md) | Backend REST API endpoints and schemas |
| [Domain Models](docs/models/domain-models.md) | Kotlin data class definitions |
| [Screen Specs](docs/design/screen-specs.md) | Screen-by-screen UI specifications |
| [Milestones](docs/milestones.md) | Development phases and task breakdown |
| [WMO Weather Codes](docs/wmo-weather-codes.md) | Weather code → description/icon mapping |

---

## Screenshots

> *Screenshots will be added after the first beta build.*

<!-- 
<div align="center">
<img src="store-listing/screenshots/home.png" width="250" />
<img src="store-listing/screenshots/forecast.png" width="250" />
<img src="store-listing/screenshots/settings.png" width="250" />
</div>
-->

---

## Roadmap

All 28 planned tasks are complete:

- [x] Offline caching with Room DB
- [x] Pull-to-refresh
- [x] Background sync with WorkManager
- [x] Home screen widgets (6 variants)
- [x] Widget configuration
- [x] Severe weather notifications
- [x] City search autocomplete
- [x] Multiple saved locations with pager
- [x] Location management screen
- [x] GPS location detection
- [x] Unit preferences (temp, wind, precip, time)
- [x] Dark / Light / System theme
- [x] Hourly forecast strip
- [x] 10-day daily forecast card
- [x] UV index card
- [x] Wind, Humidity, Pressure, Visibility cards
- [x] Sunrise/Sunset card with sun arc
- [x] Feels Like card
- [x] Air Quality card (AQI)
- [x] Precipitation nowcast
- [x] Weather alert banner
- [x] Accessibility (content descriptions)
- [x] Unit & integration tests
- [x] Backend — Node.js proxy with caching, rate limiting, Swagger
- [x] Dynamic theming (Material You)
- [x] Animations & transitions
- [x] i18n / localization (EN, ES, MS)
- [x] README overhaul

**Potential future enhancements:**
- Wear OS companion app
- Radar map overlay (MapBox/Google Maps)
- "On This Day" historical comparisons
- Pollen forecast details
- Home screen widget configuration activity
- Additional language translations
- Google Play release with premium features

---

## Contributing

This is currently a personal project. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## Acknowledgments

- **[Open-Meteo](https://open-meteo.com)** — Free, open-source weather API aggregating 15+ national weather models
- **[Material Design 3](https://m3.material.io)** — Google's design system for adaptive, accessible UI
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** — Android's modern declarative UI toolkit
- **[Express.js](https://expressjs.com)** — Fast, minimal Node.js web framework

---

## License

Personal use. Weather data provided by [Open-Meteo.com](https://open-meteo.com), licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
