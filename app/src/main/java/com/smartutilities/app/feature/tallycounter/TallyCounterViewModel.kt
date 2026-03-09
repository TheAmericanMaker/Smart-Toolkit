package com.smartutilities.app.feature.tallycounter

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TallyCounterUiState(
    val count: Int = 0
)

@HiltViewModel
class TallyCounterViewModel @Inject constructor(
    application: Application
) : ViewModel() {

    private val prefs = application.getSharedPreferences("tally_counter", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(TallyCounterUiState(count = prefs.getInt("count", 0)))
    val uiState: StateFlow<TallyCounterUiState> = _uiState.asStateFlow()

    fun increment() {
        val newCount = _uiState.value.count + 1
        _uiState.value = TallyCounterUiState(count = newCount)
        prefs.edit().putInt("count", newCount).apply()
    }

    fun decrement() {
        val newCount = maxOf(0, _uiState.value.count - 1)
        _uiState.value = TallyCounterUiState(count = newCount)
        prefs.edit().putInt("count", newCount).apply()
    }

    fun reset() {
        _uiState.value = TallyCounterUiState(count = 0)
        prefs.edit().putInt("count", 0).apply()
    }
}
