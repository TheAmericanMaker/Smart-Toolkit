package com.smarttoolkit.app.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.db.HistoryDao
import com.smarttoolkit.app.data.db.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val isScientific: Boolean = false
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    private val engine = CalculatorEngine()
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("calculator")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        val expr = _uiState.value.expression
        val result = engine.evaluate(expr)
        _uiState.value = _uiState.value.copy(result = result)
        if (result != "Error" && expr.isNotBlank()) {
            viewModelScope.launch {
                historyDao.insert(
                    HistoryEntry(
                        featureKey = "calculator",
                        label = "$expr = $result",
                        value = result
                    )
                )
            }
        }
    }

    fun toggleScientific() {
        _uiState.value = _uiState.value.copy(isScientific = !_uiState.value.isScientific)
    }

    fun onHistoryItemClick(entry: HistoryEntry) {
        _uiState.value = _uiState.value.copy(expression = entry.value, result = "")
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearFeature("calculator") }
    }
}
