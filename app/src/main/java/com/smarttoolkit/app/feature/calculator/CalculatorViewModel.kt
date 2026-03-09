package com.smarttoolkit.app.feature.calculator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val isScientific: Boolean = false
)

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val engine = CalculatorEngine()
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onInput(value: String) {
        _uiState.value = _uiState.value.copy(
            expression = _uiState.value.expression + value
        )
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(expression = "", result = "")
    }

    fun onBackspace() {
        val expr = _uiState.value.expression
        if (expr.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(expression = expr.dropLast(1))
        }
    }

    fun onEquals() {
        val result = engine.evaluate(_uiState.value.expression)
        _uiState.value = _uiState.value.copy(result = result)
    }

    fun toggleScientific() {
        _uiState.value = _uiState.value.copy(isScientific = !_uiState.value.isScientific)
    }
}
