# Development Milestones

## Phase 0: Planning (Complete)
- [x] Define architecture and tech stack
- [x] Design backend API contract
- [x] Define domain models
- [x] Create screen specifications
- [x] Map WMO weather codes
- [x] Create development roadmap
- [x] Create .env template for backend
- [x] Review and finalize all planning docs

---

## Phase 1: Backend Server — Foundation (Complete)
**Goal**: Working backend that proxies Open-Meteo data with caching.

- [x] Initialize Node.js project (ES modules, Express)
- [x] Create project structure (routes, services, middleware, utils)
- [x] Implement `openMeteoService.js` — Forecast API client
- [x] Implement `openMeteoService.js` — Air Quality API client
- [x] Implement `openMeteoService.js` — Geocoding API client
- [x] Implement `transformService.js` — Raw response → app-friendly schema
- [x] Implement `weatherCodes.js` — WMO code mapping utility
- [x] Implement `cacheService.js` — In-memory TTL cache (15 min)
- [x] Build `GET /api/weather` route
- [x] Build `GET /api/air-quality` route
- [x] Build `GET /api/geocoding/search` route
- [x] Build `GET /api/weather/combined` route
- [x] Add rate limiter middleware
- [x] Add global error handler middleware
- [x] Create `.env` config with validation
- [x] Test all endpoints manually (curl/Postman)
- [x] Write basic integration tests

**Deliverable**: Backend running on `localhost:3000` serving transformed weather data.

---

## Phase 2: Android App — Project Setup & Architecture
**Goal**: Android project scaffolded with all dependencies, DI, and data layer.

- [x] Create Android project in Android Studio (Kotlin, Compose, min SDK 26)
- [x] Configure `build.gradle.kts` with all dependencies
- [x] Set up Hilt dependency injection
- [x] Define domain models (data classes from planning doc)
- [x] Set up Retrofit + OkHttp for networking
- [x] Define API service interfaces
- [x] Create DTO classes with Kotlin Serialization
- [x] Create DTO → Domain mappers
- [x] Set up Room database (entities, DAOs, database class)
- [x] Set up DataStore for preferences
- [x] Implement repository interfaces (domain layer)
- [x] Implement repository (data layer — remote + local)
- [x] Create use cases (GetWeather, GetAirQuality, SearchLocations, etc.)
- [x] Set up Compose Navigation (type-safe)
- [x] Create theme (Material 3, dynamic color, weather palettes)

**Deliverable**: App compiles, Hilt injects dependencies, data flows from backend → Room.

---

## Phase 3: Android App — Core UI
**Goal**: Main weather screen displays real data with polished UI.

- [x] Build Home screen scaffold (collapsible header + scrollable content)
- [x] Implement dynamic gradient backgrounds (weather + time based)
- [x] Build current conditions hero (temp, description, high/low)
- [x] Build hourly forecast horizontal strip
- [x] Build 10-day daily forecast with temperature bars
- [x] Build UV Index card with visual scale
- [x] Build Wind card with compass rose
- [x] Build Air Quality card with colored AQI indicator
- [x] Build Humidity & Dew Point card
- [x] Build Pressure card with trend
- [x] Build Sunrise/Sunset card with arc visualization
- [x] Build Visibility card
- [x] Build Feels Like card
- [x] Build precipitation nowcast bar chart (15-min data)
- [x] Implement pull-to-refresh
- [x] Implement loading and error states
- [x] Add weather icons (animated or static)
- [x] Implement HomeViewModel with StateFlow

**Deliverable**: Home screen renders full weather data for a hardcoded location.

---

## Phase 4: Android App — Location Features
**Goal**: GPS detection, city search, multiple locations with swipe navigation.

- [x] Implement GPS location detection (FusedLocationProvider)
- [x] Handle location permission flow (request, rationale, settings redirect)
- [x] Build Search screen with debounced text input
- [x] Integrate Geocoding API for search results
- [x] Build Location Management screen (list, reorder, delete)
- [x] Implement SavedLocation persistence in Room
- [x] Implement horizontal paging (swipe between locations)
- [x] Add location page dots indicator
- [x] Handle "no locations saved" state (redirect to search)

**Deliverable**: User can detect GPS location, search cities, save favorites, and swipe between them.

---

## Phase 5: Android Widgets (Jetpack Glance)
**Goal**: 4 widget sizes rendering weather data on the home screen.

- [x] Set up Glance dependencies
- [x] Create `SmallWeatherWidget` (2×1 — temp + icon + name)
- [x] Create `MediumWeatherWidget` (3×2 — current + 4hr forecast)
- [x] Create `LargeWeatherWidget` (4×3 — current + hourly + 5-day)
- [x] Create `XLWeatherWidget` (4×4 — full dashboard)
- [x] Implement `WidgetUpdateWorker` (WorkManager, 30-60 min interval)
- [x] Apply Material You dynamic theming to widgets
- [x] Create widget picker previews (Glance generated previews)
- [x] Implement weather-condition-based widget backgrounds
- [x] Handle error states and stale data display in widgets
- [x] Tap interaction → deep link to app

**Deliverable**: All 4 widget sizes installable on home screen, updating periodically.

---

## Phase 6: Offline Support & Settings
**Goal**: App works offline and respects user preferences.

- [x] Implement offline detection (ConnectivityManager)
- [x] Display cached data when offline with "Last updated" indicator
- [x] Queue refresh requests when connectivity resumes
- [x] Build Settings screen UI
- [x] Implement temperature unit switching (°C / °F)
- [x] Implement wind speed unit switching
- [x] Implement precipitation unit switching
- [x] Implement time format switching (12h / 24h)
- [x] Implement theme switching (System / Light / Dark)
- [x] Persist all settings in DataStore
- [x] Apply settings throughout all screens and widgets

**Deliverable**: App gracefully handles offline, settings persist and apply globally.

---

## Phase 7: Push Notifications & Alerts
**Goal**: Real-time severe weather alerts via push notifications.

- [x] Set up Firebase project and add `google-services.json`
- [x] Add FCM dependency to Android app
- [x] Implement `WeatherNotificationService` (FCM message receiver)
- [x] Create notification channels (Severe, Precipitation, Daily Summary)
- [x] Implement device registration flow (POST to backend)
- [x] Backend: Set up Firebase Admin SDK
- [x] Backend: Implement `alertService.js` — condition monitoring
- [x] Backend: Implement `notificationService.js` — FCM sender
- [x] Backend: Create `weatherMonitor.js` — periodic job (every 15 min)
- [x] Backend: SQLite device registration table
- [x] Backend: Build device registration routes (POST, PUT, DELETE)
- [x] Implement alert banner on Home screen
- [x] Build Alert Detail screen
- [x] Deep-link from notification → Alert Detail
- [x] De-duplication: don't re-alert for same event

**Deliverable**: Users receive push notifications for severe weather at saved locations.

---

## Phase 8: Polish & Testing
**Goal**: Production-quality finish.

- [x] Add animated weather icons (Lottie or Compose Canvas)
- [x] Smooth transitions and page animations
- [x] Parallax scroll effect on hero section
- [x] Accessibility audit (TalkBack, content descriptions, contrast)
- [x] Handle edge cases (no internet on first launch, API errors, empty states)
- [x] Performance profiling (no UI jank, memory leaks)
- [x] Write unit tests for ViewModels and Use Cases
- [x] Write unit tests for backend services
- [x] Write UI tests for critical flows (Compose testing)
- [x] Test on multiple screen sizes and Android versions
- [x] Open-Meteo attribution in app (required by license)
- [x] App icon and splash screen

**Deliverable**: Production-ready app.

---

## Phase 9 (Future): Premium Features
**Goal**: Monetization and advanced features.

- [x] Weather maps with precipitation radar overlay
- [x] Historical weather data ("On This Day")
- [x] Extended 16-day forecast
- [x] Pollen detailed breakdown (Europe)
- [x] Additional widget styles
- [x] Google Play Billing (one-time purchase)
- [x] Backend auth layer for premium feature gating
- [x] Google Play Store listing

---

## Timeline Estimate

| Phase | Estimated Duration |
|---|---|
| Phase 0: Planning | ✅ Complete |
| Phase 1: Backend Foundation | ✅ Complete |
| Phase 2: Android Setup & Architecture | ✅ Complete |
| Phase 3: Core UI | ✅ Complete |
| Phase 4: Location Features | ✅ Complete |
| Phase 5: Widgets | ✅ Complete |
| Phase 6: Offline & Settings | ✅ Complete |
| Phase 7: Notifications & Alerts | ✅ Complete |
| Phase 8: Polish & Testing | ✅ Complete |
| Phase 9: Premium Features | ✅ Complete |
| **Total (Phases 0-9)** | **✅ All Complete** |

---

## Post-Release: Backend Hardening (Complete)
**Goal**: Production-ready security, reliability, and observability improvements.

- [x] Install and wire up `helmet` for security headers
- [x] Install and wire up `cors` with configurable allowed origins
- [x] Install and wire up `compression` (gzip/brotli)
- [x] Add request body size limit (`express.json({ limit })`)
- [x] Add request ID middleware (`crypto.randomUUID`)
- [x] Add structured request logging middleware (method, URL, status, duration, IP)
- [x] Add 404 catch-all handler with JSON response
- [x] Add configurable trust proxy setting
- [x] Improve graceful shutdown (SIGINT, unhandledRejection, uncaughtException, drain timeout)
- [x] Add server shutdown timeout configuration
- [x] Create project-level `.gitignore`
- [x] Update `.env` and `.env.example` with new config options
- [x] Run `npm audit fix` (resolved qs vulnerability)
- [x] Verify all 138 tests still pass
