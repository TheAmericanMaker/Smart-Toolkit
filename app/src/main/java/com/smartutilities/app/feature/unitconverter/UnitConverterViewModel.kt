package com.smartutilities.app.feature.unitconverter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UnitConverterUiState(
    val categoryIndex: Int = 0,
    val fromUnitIndex: Int = 0,
    val toUnitIndex: Int = 1,
    val inputValue: String = "",
    val result: String = ""
) {
    val category: UnitCategory get() = unitCategories[categoryIndex]
    val fromUnit: UnitDef get() = category.units[fromUnitIndex]
    val toUnit: UnitDef get() = category.units[toUnitIndex]
}

@HiltViewModel
class UnitConverterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    fun selectCategory(index: Int) {
        _uiState.value = _uiState.value.copy(
            categoryIndex = index, fromUnitIndex = 0, toUnitIndex = 1, inputValue = "", result = ""
        )
    }

    fun selectFromUnit(index: Int) {
        _uiState.value = _uiState.value.copy(fromUnitIndex = index)
        convert()
    }

    fun selectToUnit(index: Int) {
        _uiState.value = _uiState.value.copy(toUnitIndex = index)
        convert()
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(inputValue = value)
        convert()
    }

    fun swap() {
        val s = _uiState.value
        _uiState.value = s.copy(fromUnitIndex = s.toUnitIndex, toUnitIndex = s.fromUnitIndex)
        convert()
    }

    private fun convert() {
        val s = _uiState.value
        val input = s.inputValue.toDoubleOrNull()
        if (input == null) {
            _uiState.value = s.copy(result = "")
            return
        }
        val baseValue = s.fromUnit.toBase(input)
        val result = s.toUnit.fromBase(baseValue)
        _uiState.value = s.copy(result = "%.6g".format(result))
    }
}
