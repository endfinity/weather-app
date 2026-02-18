package com.clearsky.weather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.GeocodingResult
import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.model.SearchUiState
import com.clearsky.weather.domain.usecase.SaveLocationUseCase
import com.clearsky.weather.domain.usecase.SearchLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLocations: SearchLocationsUseCase,
    private val saveLocation: SaveLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val _locationSaved = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val locationSaved: MutableSharedFlow<Boolean> = _locationSaved

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }

        if (query.length < 2) {
            _uiState.update { it.copy(results = emptyList(), error = null, isSearching = false) }
            return
        }

        _searchQuery.tryEmit(query)
    }

    fun onClearQuery() {
        _uiState.update { SearchUiState() }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, error = null) }

        searchLocations(query)
            .onSuccess { results ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        results = results,
                        error = if (results.isEmpty()) "No cities found for \"$query\"" else null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = error.message ?: "Search failed"
                    )
                }
            }
    }

    fun onLocationSelected(result: GeocodingResult) {
        viewModelScope.launch {
            val location = SavedLocation(
                name = result.name,
                latitude = result.latitude,
                longitude = result.longitude,
                countryCode = result.countryCode,
                admin1 = result.admin1,
                timezone = result.timezone,
                isCurrentLocation = false,
                sortOrder = Int.MAX_VALUE,
                addedAt = System.currentTimeMillis()
            )
            saveLocation(location)
            _locationSaved.tryEmit(true)
        }
    }
}
