package com.clearsky.weather.ui.historical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.OnThisDayData
import com.clearsky.weather.domain.model.SavedLocation
import com.clearsky.weather.domain.repository.PremiumRepository
import com.clearsky.weather.domain.usecase.GetOnThisDayUseCase
import com.clearsky.weather.domain.usecase.GetSavedLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnThisDayUiState(
    val isLoading: Boolean = true,
    val data: OnThisDayData? = null,
    val error: String? = null,
    val locationName: String = ""
)

@HiltViewModel
class OnThisDayViewModel @Inject constructor(
    private val getOnThisDay: GetOnThisDayUseCase,
    private val getSavedLocations: GetSavedLocationsUseCase,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnThisDayUiState())
    val uiState: StateFlow<OnThisDayUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val locations = getSavedLocations().first()
            val location = locations.firstOrNull()

            if (location == null) {
                _uiState.update { it.copy(isLoading = false, error = "No saved locations") }
                return@launch
            }

            _uiState.update { it.copy(locationName = location.name) }

            getOnThisDay(
                latitude = location.latitude,
                longitude = location.longitude
            ).onSuccess { data ->
                _uiState.update { it.copy(isLoading = false, data = data) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
