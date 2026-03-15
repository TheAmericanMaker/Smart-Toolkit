package com.smarttoolkit.app.feature.tallycounter

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

data class TallyCounterUiState(
    val count: Int = 0
)

@HiltViewModel
class TallyCounterViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TallyCounterUiState())
    val uiState: StateFlow<TallyCounterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = prefs.tallyCount.first()
            _uiState.value = TallyCounterUiState(count = saved)
        }
    }

    fun increment() {
        val newCount = _uiState.value.count + 1
        _uiState.value = TallyCounterUiState(count = newCount)
        viewModelScope.launch { prefs.setTallyCount(newCount) }
    }

    fun decrement() {
        val newCount = maxOf(0, _uiState.value.count - 1)
        _uiState.value = TallyCounterUiState(count = newCount)
        viewModelScope.launch { prefs.setTallyCount(newCount) }
    }

    fun reset() {
        _uiState.value = TallyCounterUiState(count = 0)
        viewModelScope.launch { prefs.setTallyCount(0) }
    }
}
