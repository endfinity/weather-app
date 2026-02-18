package com.clearsky.weather.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.RadarData
import com.clearsky.weather.domain.model.RadarFrame
import com.clearsky.weather.domain.usecase.GetRadarDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RadarUiState(
    val isLoading: Boolean = true,
    val data: RadarData? = null,
    val error: String? = null,
    val currentFrameIndex: Int = 0,
    val isPlaying: Boolean = false,
    val allFrames: List<RadarFrame> = emptyList()
)

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val getRadarData: GetRadarDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private var animationJob: Job? = null

    companion object {
        private const val FRAME_DELAY_MS = 500L
    }

    init {
        loadRadarData()
    }

    fun loadRadarData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getRadarData().onSuccess { data ->
                val allFrames = data.pastFrames + data.nowcastFrames
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        data = data,
                        allFrames = allFrames,
                        currentFrameIndex = 0
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun togglePlayback() {
        val currentState = _uiState.value
        if (currentState.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _uiState.update { it.copy(isPlaying = true) }

        animationJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val state = _uiState.value
                val nextIndex = (state.currentFrameIndex + 1) % state.allFrames.size
                _uiState.update { it.copy(currentFrameIndex = nextIndex) }
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun stopPlayback() {
        animationJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekToFrame(index: Int) {
        _uiState.update { it.copy(currentFrameIndex = index) }
    }

    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }
}
