package com.smarttoolkit.app.feature.ruler

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RulerUiState(
    val isMetric: Boolean = true
)

@HiltViewModel
class RulerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RulerUiState())
    val uiState: StateFlow<RulerUiState> = _uiState.asStateFlow()

    fun toggleUnit() {
        _uiState.value = _uiState.value.copy(isMetric = !_uiState.value.isMetric)
    }
}
