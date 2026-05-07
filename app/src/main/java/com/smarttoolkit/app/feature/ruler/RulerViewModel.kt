package com.smarttoolkit.app.feature.ruler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulerUiState(
    val isMetric: Boolean = true,
    val dpiOffset: Float = 0f,
    val showCalibration: Boolean = false
)

@HiltViewModel
class RulerViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulerUiState())
    val uiState: StateFlow<RulerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val offset = prefs.rulerDpiOffset.first()
            _uiState.value = _uiState.value.copy(dpiOffset = offset)
        }
    }

    fun toggleUnit() {
        _uiState.value = _uiState.value.copy(isMetric = !_uiState.value.isMetric)
    }

    fun toggleCalibration() {
        _uiState.value = _uiState.value.copy(showCalibration = !_uiState.value.showCalibration)
    }

    fun setDpiOffset(offset: Float) {
        _uiState.value = _uiState.value.copy(dpiOffset = offset)
        viewModelScope.launch { prefs.setRulerDpiOffset(offset) }
    }
}
