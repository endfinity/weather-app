# Architecture

## System Overview

ClearSky uses a three-tier architecture: **Android App → Backend Server → Open-Meteo APIs**

The backend acts as a proxy, aggregation, and notification layer. The Android app never calls Open-Meteo directly — this allows us to:
1. Combine multiple API calls into a single response (forecast + air quality)
2. Cache responses server-side to stay within Open-Meteo's 10,000 calls/day free limit
3. Transform raw data into an app-optimized schema
4. Monitor weather conditions and trigger push notifications
5. Add auth/feature-gating later for monetization

---

## Data Flow

```
User opens app
    │
    ▼
Android App checks Room cache
    │
    ├─ Cache valid (< 30 min old) → Display cached data
    │
    └─ Cache stale or missing
           │
           ▼
       GET /api/weather?lat=X&lon=Y&units=metric
           │
           ▼
       Backend checks server cache
           │
           ├─ Cache valid (< 15 min) → Return cached response
           │
           └─ Cache miss
                  │
                  ▼
              Parallel fetch:
              ├─ Open-Meteo Forecast API
              └─ Open-Meteo Air Quality API
                  │
                  ▼
              Transform & merge responses
                  │
                  ▼
              Cache on server → Return to app
                  │
                  ▼
              App persists to Room → Display to user
```

---

## Android Architecture (Clean Architecture + MVVM)

```
┌──────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                     │
│                                                          │
│  ┌────────────┐    ┌─────────────┐    ┌───────────────┐  │
│  │  Screens   │◀──▶│ ViewModels  │◀──▶│  UI State     │  │
│  │ (Compose)  │    │  (Hilt)     │    │  (StateFlow)  │  │
│  └────────────┘    └──────┬──────┘    └───────────────┘  │
│                           │                              │
├───────────────────────────┼──────────────────────────────┤
│                    DOMAIN LAYER                          │
│                           │                              │
│  ┌────────────┐    ┌──────┴──────┐    ┌───────────────┐  │
│  │  Models    │    │  Use Cases  │    │  Repository   │  │
│  │ (data cls) │    │             │    │  (interface)  │  │
│  └────────────┘    └─────────────┘    └───────┬───────┘  │
│                                               │          │
├───────────────────────────────────────────────┼──────────┤
│                     DATA LAYER                │          │
│                                               │          │
│  ┌────────────┐    ┌─────────────┐    ┌───────┴───────┐  │
│  │  Remote    │    │   Local     │    │  Repository   │  │
│  │  (Retrofit)│    │   (Room)    │    │  (impl)       │  │
│  └────────────┘    └─────────────┘    └───────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**Presentation Layer**
- Compose screens and UI components
- ViewModels holding UI state as `StateFlow`
- Navigation logic
- Theme and styling
- No business logic — purely reactive display

**Domain Layer**
- Pure Kotlin data classes (no Android dependencies)
- Use cases encapsulating single business operations
- Repository interfaces (contracts)
- Unit-testable without Android framework

**Data Layer**
- Repository implementations
- Retrofit API service definitions
- Room database, DAOs, entities
- DataStore for preferences
- DTO ↔ Domain model mappers

---

## Backend Architecture

```
backend/
├── src/
│   ├── index.js                    # Express app entry, middleware setup
│   ├── config.js                   # Environment config
│   ├── routes/
│   │   ├── weather.js              # GET /api/weather
│   │   ├── airQuality.js           # GET /api/air-quality
│   │   ├── geocoding.js            # GET /api/geocoding/search
│   │   └── devices.js              # POST /api/devices/register, DELETE /api/devices/:id
│   ├── services/
│   │   ├── openMeteoService.js     # Open-Meteo API client (forecast + AQ + geocoding)
│   │   ├── cacheService.js         # In-memory cache with TTL
│   │   ├── alertService.js         # Severe weather monitoring logic
│   │   ├── notificationService.js  # FCM push notification sender
│   │   └── transformService.js     # Raw API → app-friendly schema transform
│   ├── middleware/
│   │   ├── rateLimiter.js          # Rate limiting per IP
│   │   └── errorHandler.js         # Global error handler
│   ├── jobs/
│   │   └── weatherMonitor.js       # Periodic job: check conditions → send alerts
│   └── utils/
│       ├── weatherCodes.js         # WMO code → description + icon mapping
│       └── units.js                # Unit conversion helpers
├── db/
│   └── schema.sql                  # SQLite schema for device registrations
├── package.json
├── .env.example
└── Dockerfile
```

---

## Widget Architecture (Jetpack Glance)

```
Widget Update Flow:

WorkManager (every 30-60 min)
    │
    ▼
WidgetUpdateWorker
    │
    ├─ Read cached weather from Room
    │
    ├─ If stale → Fetch fresh data from backend
    │
    └─ Update GlanceAppWidget state
           │
           ▼
       Glance composables re-render
```

### Widget Sizes
| Size | Grid | Content |
|---|---|---|
| Small | 2×1 | Temp + icon + location |
| Medium | 3×2 | Current + next 4 hours |
| Large | 4×3 | Current + hourly strip + 5-day forecast |
| XL | 4×4 | Full dashboard (current, hourly, daily, AQI, UV, wind) |

---

## Caching Strategy

| Layer | Storage | TTL | Purpose |
|---|---|---|---|
| Backend server | node-cache (in-memory) | 15 minutes | Reduce redundant Open-Meteo calls |
| Android app | Room database | 30 minutes | Offline support, app restart persistence |
| Android prefs | DataStore | Indefinite | User settings, saved locations |
| Widgets | Room (shared) | 30-60 minutes | Widget data driven by WorkManager refresh |

---

## Notification Strategy

```
Backend Weather Monitor (runs every 15 min)
    │
    ▼
For each registered device/location:
    │
    ├─ Fetch latest weather
    │
    ├─ Check for severe conditions:
    │   ├─ WMO codes ≥ 95 (thunderstorm)
    │   ├─ Temperature extremes (< -20°C or > 40°C)
    │   ├─ Wind gusts > 90 km/h
    │   ├─ Heavy precipitation (> 20mm/hr)
    │   ├─ UV Index ≥ 8 (Very High)
    │   └─ AQI ≥ 151 (Unhealthy)
    │
    └─ Send FCM push notification if triggered
        (with de-duplication: don't re-alert for same event)
```

---

## Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| API proxy vs direct | Proxy through backend | Rate limit control, data aggregation, push notification infra, future monetization |
| Open-Meteo vs others | Open-Meteo | Most accurate free option: 15+ models, 1-2km resolution, no API key, open-source |
| Compose vs XML | Jetpack Compose | Modern, less boilerplate, Material 3 native, recommended by Google |
| Glance vs RemoteViews | Jetpack Glance | Compose syntax for widgets, Material You, generated previews |
| Room vs file cache | Room | Structured queries, type-safe, works with Flow/coroutines |
| Kotlin Serialization vs Gson | Kotlin Serialization | Kotlin-native, compile-time safe, smaller than Gson |
| Hilt vs Koin | Hilt | Compile-time DI, first-party Jetpack support, better for large projects |
| SQLite vs Postgres (backend) | SQLite | Lightweight for personal use; upgrade path to Postgres if scaling |
