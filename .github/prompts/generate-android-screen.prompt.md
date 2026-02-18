---
mode: 'agent'
tools: ['codebase', 'terminal']
description: 'Generate a new Android screen with ViewModel, UiState, and Composables following MVVM + Clean Architecture'
---

# Generate Android Screen

Generate a complete Android screen implementation following the project's MVVM + Clean Architecture patterns.

## Screen Details

- **Screen Name**: `{{screenName}}`
- **Description**: `{{description}}`
- **Navigation Route**: `{{route}}`

## Requirements

Generate the following files for the new screen:

### 1. UI State

`kotlin
// feature/{{featureName}}/{{screenName}}UiState.kt
data class {{screenName}}UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Add screen-specific state fields
)
`

### 2. UI Actions

`kotlin
// feature/{{featureName}}/{{screenName}}Action.kt
sealed interface {{screenName}}Action {
    // Add screen-specific actions
}
`

### 3. ViewModel

`kotlin
// feature/{{featureName}}/{{screenName}}ViewModel.kt
@HiltViewModel
class {{screenName}}ViewModel @Inject constructor(
    // Inject required use cases or repositories
) : ViewModel() {

    private val _uiState = MutableStateFlow({{screenName}}UiState())
    val uiState: StateFlow<{{screenName}}UiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = {{screenName}}UiState()
        )

    fun onAction(action: {{screenName}}Action) {
        when (action) {
            // Handle actions
        }
    }
}
`

### 4. Screen Composable

`kotlin
// feature/{{featureName}}/{{screenName}}Screen.kt
@Composable
fun {{screenName}}Screen(
    onNavigateBack: () -> Unit,
    viewModel: {{screenName}}ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    {{screenName}}Content(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun {{screenName}}Content(
    uiState: {{screenName}}UiState,
    onAction: ({{screenName}}Action) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("{{screenName}}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.errorMessage != null -> ErrorContent(message = uiState.errorMessage)
                else -> { /* Main content */ }
            }
        }
    }
}
`

### 5. Preview

`kotlin
@Preview(showBackground = true)
@Composable
private fun {{screenName}}ContentPreview() {
    ClearSkyTheme {
        {{screenName}}Content(
            uiState = {{screenName}}UiState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
`

### 6. Navigation Integration

Add navigation route and composable to the app's NavHost:

`kotlin
composable("{{route}}") {
    {{screenName}}Screen(
        onNavigateBack = { navController.popBackStack() }
    )
}
`

## Rules

- Use `collectAsStateWithLifecycle()` (NOT `collectAsState()`)
- Pass `Modifier` as the last parameter on content composables
- Use `hiltViewModel()` only in the stateful wrapper
- Keep the content composable stateless and previewable
- Follow Material 3 design guidelines
- Include accessibility content descriptions for all icons
- Handle loading, error, and success states
