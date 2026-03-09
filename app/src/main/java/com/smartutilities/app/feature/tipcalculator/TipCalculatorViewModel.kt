package com.smartutilities.app.feature.tipcalculator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TipCalculatorUiState(
    val billAmount: String = "",
    val tipPercentage: Int = 15,
    val customTipText: String = "",
    val isCustomTip: Boolean = false,
    val numberOfPeople: Int = 1,
    val tipAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val perPersonAmount: Double = 0.0
)

@HiltViewModel
class TipCalculatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TipCalculatorUiState())
    val uiState: StateFlow<TipCalculatorUiState> = _uiState.asStateFlow()

    fun onBillAmountChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        _uiState.value = _uiState.value.copy(billAmount = filtered)
        recalculate()
    }

    fun onTipPercentageSelected(percent: Int) {
        _uiState.value = _uiState.value.copy(
            tipPercentage = percent,
            isCustomTip = false
        )
        recalculate()
    }

    fun onCustomTipChanged(text: String) {
        val filtered = text.filter { it.isDigit() }
        val percent = filtered.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            customTipText = filtered,
            tipPercentage = percent,
            isCustomTip = true
        )
        recalculate()
    }

    fun onPeopleIncrement() {
        _uiState.value = _uiState.value.copy(
            numberOfPeople = minOf(99, _uiState.value.numberOfPeople + 1)
        )
        recalculate()
    }

    fun onPeopleDecrement() {
        _uiState.value = _uiState.value.copy(
            numberOfPeople = maxOf(1, _uiState.value.numberOfPeople - 1)
        )
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val bill = state.billAmount.toDoubleOrNull() ?: 0.0
        val tip = bill * state.tipPercentage / 100.0
        val total = bill + tip
        val perPerson = if (state.numberOfPeople > 0) total / state.numberOfPeople else total
        _uiState.value = state.copy(
            tipAmount = tip,
            totalAmount = total,
            perPersonAmount = perPerson
        )
    }
}
