package com.smarttoolkit.app.feature.magnifyingglass

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MagnifyingGlassUiState(
    val zoomRatio: Float = 1f,
    val minZoom: Float = 1f,
    val maxZoom: Float = 1f,
    val isTorchOn: Boolean = false
)

@HiltViewModel
class MagnifyingGlassViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MagnifyingGlassUiState())
    val uiState: StateFlow<MagnifyingGlassUiState> = _uiState.asStateFlow()

    fun onZoomChanged(ratio: Float) {
        _uiState.value = _uiState.value.copy(
            zoomRatio = ratio.coerceIn(_uiState.value.minZoom, _uiState.value.maxZoom)
        )
    }

    fun onZoomRangeDetected(min: Float, max: Float) {
        _uiState.value = _uiState.value.copy(minZoom = min, maxZoom = max)
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchOn = !_uiState.value.isTorchOn)
    }
}
