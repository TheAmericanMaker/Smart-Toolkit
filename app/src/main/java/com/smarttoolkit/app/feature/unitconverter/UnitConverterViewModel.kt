package com.smarttoolkit.app.feature.unitconverter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.db.HistoryDao
import com.smarttoolkit.app.data.db.HistoryEntry
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
class UnitConverterViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("unitconverter")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val cat = prefs.ucCategory.first()
            val from = prefs.ucFromUnit.first()
            val to = prefs.ucToUnit.first()
            // Validate indices against available categories/units
            val safeCat = cat.coerceIn(0, unitCategories.lastIndex)
            val maxUnit = unitCategories[safeCat].units.lastIndex
            val safeFrom = from.coerceIn(0, maxUnit)
            val safeTo = to.coerceIn(0, maxUnit)
            _uiState.value = _uiState.value.copy(
                categoryIndex = safeCat,
                fromUnitIndex = safeFrom,
                toUnitIndex = safeTo
            )
        }
    }

    private fun persistSelections() {
        val s = _uiState.value
        viewModelScope.launch { prefs.setUcSelections(s.categoryIndex, s.fromUnitIndex, s.toUnitIndex) }
    }

    fun selectCategory(index: Int) {
        _uiState.value = _uiState.value.copy(
            categoryIndex = index, fromUnitIndex = 0, toUnitIndex = 1, inputValue = "", result = ""
        )
        persistSelections()
    }

    fun selectFromUnit(index: Int) {
        _uiState.value = _uiState.value.copy(fromUnitIndex = index)
        persistSelections()
        convert()
    }

    fun selectToUnit(index: Int) {
        _uiState.value = _uiState.value.copy(toUnitIndex = index)
        persistSelections()
        convert()
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(inputValue = value)
        convert()
    }

    fun swap() {
        val s = _uiState.value
        _uiState.value = s.copy(fromUnitIndex = s.toUnitIndex, toUnitIndex = s.fromUnitIndex)
        persistSelections()
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
        val formatted = "%.6g".format(result)
        _uiState.value = s.copy(result = formatted)

        // Save to history
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntry(
                    featureKey = "unitconverter",
                    label = "${s.inputValue} ${s.fromUnit.symbol} = $formatted ${s.toUnit.symbol}",
                    value = formatted
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearFeature("unitconverter") }
    }
}
