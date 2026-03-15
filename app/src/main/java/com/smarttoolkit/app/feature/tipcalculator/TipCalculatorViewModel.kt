package com.smarttoolkit.app.feature.tipcalculator

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

enum class RoundingMode { NONE, ROUND_TOTAL, ROUND_PER_PERSON }

data class TipCalculatorUiState(
    val billAmount: String = "",
    val taxAmount: String = "",
    val tipPercentage: Int = 15,
    val customTipText: String = "",
    val isCustomTip: Boolean = false,
    val numberOfPeople: Int = 1,
    val roundingMode: RoundingMode = RoundingMode.NONE,
    val tipAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val perPersonAmount: Double = 0.0
)

@HiltViewModel
class TipCalculatorViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TipCalculatorUiState())
    val uiState: StateFlow<TipCalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedTip = prefs.tipPercentage.first()
            _uiState.value = _uiState.value.copy(tipPercentage = savedTip)
        }
    }

    fun onBillAmountChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        _uiState.value = _uiState.value.copy(billAmount = filtered)
        recalculate()
    }

    fun onTaxAmountChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        _uiState.value = _uiState.value.copy(taxAmount = filtered)
        recalculate()
    }

    fun setRoundingMode(mode: RoundingMode) {
        _uiState.value = _uiState.value.copy(roundingMode = mode)
        recalculate()
    }

    fun onTipPercentageSelected(percent: Int) {
        _uiState.value = _uiState.value.copy(
            tipPercentage = percent,
            isCustomTip = false
        )
        viewModelScope.launch { prefs.setTipPercentage(percent) }
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
        viewModelScope.launch { prefs.setTipPercentage(percent) }
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
        val tax = state.taxAmount.toDoubleOrNull() ?: 0.0
        val tip = bill * state.tipPercentage / 100.0
        var total = bill + tax + tip
        var perPerson = if (state.numberOfPeople > 0) total / state.numberOfPeople else total

        when (state.roundingMode) {
            RoundingMode.ROUND_TOTAL -> {
                total = kotlin.math.ceil(total)
                perPerson = if (state.numberOfPeople > 0) total / state.numberOfPeople else total
            }
            RoundingMode.ROUND_PER_PERSON -> {
                perPerson = kotlin.math.ceil(perPerson)
                total = perPerson * state.numberOfPeople
            }
            RoundingMode.NONE -> {}
        }

        _uiState.value = state.copy(
            tipAmount = tip,
            totalAmount = total,
            perPersonAmount = perPerson
        )
    }
}
