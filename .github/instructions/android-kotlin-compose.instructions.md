---
description: 'Comprehensive Android development guidelines for native apps using Kotlin, Jetpack Compose, Material 3, and modern Android architecture patterns.'
applyTo: '**/*.kt, **/*.kts, **/build.gradle*, **/AndroidManifest.xml, **/*.xml'
---

# Android Kotlin Development Instructions

Comprehensive guidelines for building native Android applications with Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt DI, Retrofit, Room, DataStore, and modern Android best practices.

## Project Configuration

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 2.x with K2 compiler
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Compose Compiler**: Gradle plugin (not legacy `kotlinCompilerExtensionVersion`)
- **Dependency Injection**: Hilt with KSP (not KAPT)
- **Async**: Kotlin Coroutines + Flow
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Local Storage**: Room + DataStore
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit 5, Turbine (Flow), MockK, Compose UI testing

## Kotlin Language Conventions

### General Rules
- Use Kotlin 2.x idioms; never write Java-style Kotlin
- Prefer `val` over `var`; immutability by default
- Use `data class` for pure data holders; `sealed class`/`sealed interface` for restricted hierarchies
- Prefer expression bodies for single-expression functions: `fun foo() = bar()`
- Use trailing commas in multi-line declarations
- Omit redundant `public` modifier; omit `Unit` return type
- Omit semicolons
- Use 4-space indentation, no tabs

### Naming Conventions
- Packages: lowercase, no underscores (`com.example.myapp`)
- Classes/Objects: UpperCamelCase (`WeatherRepository`)
- Functions/Properties: lowerCamelCase (`fetchWeatherData`)
- Constants: SCREAMING_SNAKE_CASE (`const val MAX_RETRIES = 3`)
- Backing properties: prefix with underscore (`_uiState` / `uiState`)
- Test methods: backtick names allowed (`should return weather data when API succeeds`)
- Composable functions: UpperCamelCase like classes (`WeatherScreen`, `TemperatureCard`)

### Null Safety
- NEVER use `!!` operator unless in tests or truly impossible null scenarios
- Prefer `?.let { }` or `?.run { }` for null-safe chains
- Use `requireNotNull()` or `checkNotNull()` with descriptive messages when asserting non-null
- Declare explicit Kotlin types for platform (Java interop) return values
- Use `?:` (Elvis operator) with sensible defaults: `val name = user?.name ?: "Unknown"`

### Idiomatic Patterns
- Prefer `when` over `if-else` chains for 3+ branches
- Use `if` for binary conditions, `when` for multiple options
- Prefer higher-order functions (`map`, `filter`, `flatMap`) over manual loops
- Use `also` for side effects, `let` for transformations, `apply` for configuration, `run` for scoped execution, `with` for multiple calls on same object
- Use `sealed class` with `when` for exhaustive state handling
- Prefer `listOf()`, `mapOf()`, `setOf()` (immutable) over mutable variants
- Use `buildList {}`, `buildMap {}` for conditional collection building
- Use default parameter values instead of overloaded functions
- Use named arguments for clarity when calling functions with multiple parameters of same type
- Use destructuring declarations where appropriate
- Prefer string templates over concatenation: `"Hello $
ame"`
- Use multiline strings with `trimIndent()` or `trimMargin()`

### Coroutines & Flow
- ALWAYS use structured concurrency; never use `GlobalScope`
- Use `viewModelScope` in ViewModels for coroutine launching
- Use `Flow` for reactive data streams; `StateFlow` for UI state
- Use `collectAsStateWithLifecycle()` to collect flows in Compose (lifecycle-aware)
- Use `stateIn()` with `SharingStarted.WhileSubscribed(5_000)` for converting Flow to StateFlow
- Handle cancellation properly; respect `isActive` in long-running loops
- Use `withContext(Dispatchers.IO)` for I/O operations in suspending functions
- Data layer functions should be main-safe (handle their own threading)
- Use `catch {}` operator on flows for error handling
- Prefer `flow {}` builder for cold streams, `MutableStateFlow` for hot state

## Jetpack Compose

### Core Principles
- Composables are declarative descriptions of UI, not imperative builders
- Composables must be idempotent and side-effect free
- Composable functions can execute in any order, in parallel, or be skipped
- Never perform expensive operations inside composable functions directly

### State Management
- Use `remember { mutableStateOf(default) }` for local composable state
- Use `rememberSaveable` for state that survives configuration changes
- Prefer `by` delegate syntax: `var name by remember { mutableStateOf("") }`
- NEVER use mutable collections (`ArrayList`, `mutableListOf()`) as Compose state  use `State<List<T>>` with immutable `listOf()`
- Convert `Flow` to Compose state with `collectAsStateWithLifecycle()` (preferred on Android)
- Use `derivedStateOf` ONLY when input state changes more frequently than the derived output needs to update

### State Hoisting
- Follow unidirectional data flow (UDF): state flows down, events flow up
- Stateless composables receive `value: T` and `onValueChange: (T) -> Unit` parameters
- Hoist state to the lowest common parent of all composables that use it
- Hoist state to at least the highest level it may be changed
- Screen-level composables receive state from ViewModels; inner composables are stateless
- Use plain state holder classes (`@Stable` annotated) for complex UI logic, not ViewModel

### Side Effects
- `LaunchedEffect(key)`  run suspend functions scoped to composable lifecycle; restarts when key changes
- `rememberCoroutineScope()`  obtain composition-aware scope for event handlers (e.g., `onClick`)
- `DisposableEffect(key)`  effects requiring cleanup (e.g., lifecycle observers); MUST include `onDispose {}`
- `SideEffect`  publish Compose state to non-Compose code after every successful recomposition
- `produceState`  convert non-Compose state into Compose `State<T>`
- `snapshotFlow { }`  convert Compose `State` into a Flow
- `rememberUpdatedState(value)`  reference a value in an effect that should NOT restart when value changes
- NEVER use `LaunchedEffect(true)` unless intentionally matching composable lifecycle

### Composable Organization
- One public composable per file for screens; group related private composables together
- Composable files named after the primary composable: `WeatherScreen.kt`
- Extract reusable UI into shared `components/` package
- Preview composables with `@Preview` annotation using `@PreviewParameter` for data
- Keep composables small and focused; extract sub-composables when exceeding ~40 lines
- Use `Modifier` as first optional parameter: `fun MyComposable(modifier: Modifier = Modifier)`
- Pass `Modifier` down to root layout element

### Performance
- Use `key()` in `LazyColumn`/`LazyRow` items for stable identity
- Mark classes used as composable parameters with `@Stable` or `@Immutable` when appropriate
- Avoid creating new lambda instances in tight loops; hoist lambda creation
- Defer state reads as late as possible to minimize recomposition scope
- Use `Modifier.drawBehind {}` instead of `Canvas` composable when possible
- Profile with Layout Inspector and recomposition counts

## Material 3

### Theming
- Use `MaterialTheme` composable as root with `colorScheme`, `typography`, and `shapes`
- Support dynamic color on Android 12+ with `dynamicLightColorScheme()` / `dynamicDarkColorScheme()`
- Fallback to custom `lightColorScheme()` / `darkColorScheme()` on older devices
- Use `isSystemInDarkTheme()` to determine light/dark mode
- Access theme values via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`
- Use `Surface` as backing composable for screens/containers

### Color Roles
- Use `primary` / `onPrimary` pairs together for accessible contrast
- Use `primaryContainer` / `onPrimaryContainer` for less prominent elements
- Use `surface` / `onSurface` for backgrounds and body text
- Use `surfaceVariant` / `onSurfaceVariant` for secondary surfaces
- NEVER mix container colors with wrong on-colors (e.g., `tertiaryContainer` with `onPrimary`)

### Typography
- M3 type scale: display, headline, title, body, label  large/medium/small
- Customize via `Typography` class with `TextStyle` parameters
- Access via `MaterialTheme.typography.bodyLarge` etc.
- Use font weight variations for emphasis instead of separate font families

### Components
- Use M3 components: `Button`, `Card`, `NavigationBar`, `TopAppBar`, `Scaffold`, `FloatingActionButton`
- Use component `Defaults` objects for customization: `CardDefaults.cardColors()`
- Navigate between emphasis levels: FAB > FilledButton > OutlinedButton > TextButton
- Use `NavigationBar` for compact, `NavigationRail` for medium, `NavigationDrawer` for large screens
- Use tonal elevation (`tonalElevation`) over shadow elevation for Material 3

## Architecture

### Layered Architecture (MANDATORY)
- **UI Layer**: Composables + ViewModels  displays data, handles user interactions
- **Domain Layer** (optional): Use cases/interactors  reusable business logic
- **Data Layer**: Repositories + data sources  single source of truth for data

### UI Layer
- `ViewModel` exposes UI state via `StateFlow<UiState>` and handles business logic
- UI state is a single immutable `data class` (e.g., `data class WeatherUiState(...)`)
- Use `sealed interface` for UI state when states are mutually exclusive (Loading, Success, Error)
- NEVER hold Android framework references (Context, Activity) in ViewModel  use `@ApplicationContext` via Hilt if absolutely needed
- NEVER send one-off events from ViewModel to UI; model everything as state
- Name state flows as `uiState`; use `stateIn()` for production, `MutableStateFlow` for simpler cases
- Screen composables observe `viewModel.uiState` via `collectAsStateWithLifecycle()`

### Data Layer
- Every data type gets a Repository as its single source of truth
- Repositories abstract data sources (network, database, preferences)
- Repository exposes `Flow<T>` for observable data, `suspend fun` for one-shot operations
- Repository coordinates between remote and local data sources
- Data layer types are main-safe (use `withContext(Dispatchers.IO)` internally)
- Name implementations meaningfully: `OfflineFirstWeatherRepository`, not `WeatherRepositoryImpl`
- Use `Result<T>` or custom sealed class for error handling in repository return types

### Domain Layer
- Create use cases when business logic is reused across multiple ViewModels
- Each use case = single responsibility, single public `operator fun invoke()` method
- Use cases should NOT hold mutable state
- Name descriptively: `GetWeatherForLocationUseCase`, `RefreshWeatherDataUseCase`

### Dependency Injection with Hilt
- Annotate `Application` class with `@HiltAndroidApp`
- Annotate `Activity` with `@AndroidEntryPoint`
- Annotate ViewModels with `@HiltViewModel` + `@Inject constructor(...)`
- Use constructor injection everywhere possible
- Use `@Module` + `@InstallIn` for providing interfaces and external types
- Use `@Binds` for interface-to-implementation bindings
- Use `@Provides` for external library types (Retrofit, OkHttp, Room database)
- Use `@Singleton` scope sparingly  only when shared mutable state or expensive initialization
- Use `@ViewModelScoped` for dependencies shared within a ViewModel
- Use `@ApplicationContext` / `@ActivityContext` qualifiers for Context injection
- Use KSP (not KAPT) for Hilt code generation

## Networking (Retrofit)
- Define API interfaces with suspend functions for coroutine support
- Use Kotlinx Serialization (`@Serializable` data classes) over Gson/Moshi
- Provide `Retrofit` and `OkHttpClient` instances via Hilt `@Provides`
- Use OkHttp `Interceptor` for auth headers, logging, caching
- Handle errors at the Repository level, not in the API service
- Use `@SerialName` for JSON field mapping when names differ from Kotlin properties

## Local Storage

### Room Database
- Use `@Entity` data classes for tables; `@Dao` interfaces for queries
- DAO functions should be `suspend fun` or return `Flow<T>`
- Provide database instance via Hilt with `@Singleton` scope
- Use `@TypeConverter` for complex types
- Test DAOs with in-memory database

### DataStore
- Use Preferences DataStore for simple key-value pairs
- Use Proto DataStore for typed, schema-defined data
- Access DataStore via `Flow`  never block the main thread
- Provide DataStore instance via Hilt

## Testing

### Unit Tests
- Use JUnit 5 with `@Test` annotation
- Use MockK for mocking (`mockk<T>()`, `coEvery { }`, `coVerify { }`)
- Use Turbine for testing `Flow` emissions (`flow.test { }`)
- Use `TestDispatcher` (`UnconfinedTestDispatcher` or `StandardTestDispatcher`) for coroutine testing
- Prefer fakes over mocks for repository testing
- Test ViewModels without Android dependencies
- Name tests descriptively: `should emit loading then success when data fetched`

### UI Tests
- Use Compose testing APIs: `composeTestRule.setContent { }`
- Find elements with `onNodeWithText()`, `onNodeWithContentDescription()`
- Assert with `assertIsDisplayed()`, `assertTextEquals()`
- Use `TestTag` via `Modifier.testTag("tag")` for elements without text

### Test Structure
- Follow Arrange-Act-Assert pattern
- One assertion concept per test
- Keep tests independent  no shared mutable state

## Package Structure
`
com.example.appname/
  data/
    local/
      dao/
      database/
      entity/
    remote/
      api/
      dto/
    repository/
  di/
    modules (AppModule, NetworkModule, DatabaseModule)
  domain/
    model/
    usecase/
  ui/
    components/
    navigation/
    screens/
      screenname/
        ScreenNameScreen.kt
        ScreenNameViewModel.kt
        ScreenNameUiState.kt
    theme/
      Color.kt
      Theme.kt
      Type.kt
      Shape.kt
  MainApplication.kt
  MainActivity.kt
`

## Error Handling
- Use `sealed interface` for result types (Loading, Success, Error)
- Handle errors at the repository layer with `try-catch` and `Result`
- Expose user-friendly error messages via UI state
- Log errors for debugging; never expose stack traces to users
- Use `runCatching { }` for concise try-catch in Kotlin
- Implement retry logic for transient network failures with exponential backoff

## Accessibility
- Set `contentDescription` on all `Icon` and `Image` composables
- Use `semantics { }` modifier for screen reader context
- Ensure sufficient color contrast (use M3 color roles correctly)
- Support dynamic text sizing (use `sp` for text, never hardcode heights for text containers)
- Test with TalkBack and Switch Access

## References
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Jetpack Compose State](https://developer.android.com/develop/ui/compose/state)
- [Side Effects in Compose](https://developer.android.com/develop/ui/compose/side-effects)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
