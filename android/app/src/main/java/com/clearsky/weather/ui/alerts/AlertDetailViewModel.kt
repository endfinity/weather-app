package com.clearsky.weather.ui.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.WeatherAlert
import com.clearsky.weather.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertDetailUiState(
    val alert: WeatherAlert? = null,
    val allAlerts: List<WeatherAlert> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val alertId: String = savedStateHandle.get<String>("alertId") ?: ""

    private val _uiState = MutableStateFlow(AlertDetailUiState())
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    init {
        if (alertId.isNotBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Alert details unavailable offline") }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setAlerts(alerts: List<WeatherAlert>, targetId: String) {
        val target = alerts.find { it.id == targetId }
        _uiState.update {
            it.copy(
                alert = target ?: alerts.firstOrNull(),
                allAlerts = alerts,
                isLoading = false,
                error = null
            )
        }
    }
}
