package com.clearsky.weather.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.usecase.DeleteLocationUseCase
import com.clearsky.weather.domain.usecase.GetSavedLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationManagementUiState(
    val locations: List<SavedLocation> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LocationManagementViewModel @Inject constructor(
    private val getSavedLocations: GetSavedLocationsUseCase,
    private val deleteLocation: DeleteLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationManagementUiState())
    val uiState: StateFlow<LocationManagementUiState> = _uiState.asStateFlow()

    init {
        observeLocations()
    }

    private fun observeLocations() {
        viewModelScope.launch {
            getSavedLocations().collect { locations ->
                _uiState.update {
                    it.copy(locations = locations, isLoading = false)
                }
            }
        }
    }

    fun onDeleteLocation(locationId: Long) {
        viewModelScope.launch {
            deleteLocation(locationId)
        }
    }
}
