package com.smarttoolkit.app.feature.tallycounter

import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TallyCounterStateHolder @Inject constructor(
    private val prefs: UserPreferencesRepository
) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _uiState = MutableStateFlow(TallyCounterUiState())
    val uiState: StateFlow<TallyCounterUiState> = _uiState.asStateFlow()

    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        scope.launch {
            val saved = prefs.tallyCount.first()
            _uiState.value = TallyCounterUiState(count = saved)
        }
    }

    fun increment() {
        val newCount = _uiState.value.count + 1
        _uiState.value = TallyCounterUiState(count = newCount)
        scope.launch { prefs.setTallyCount(newCount) }
    }

    fun decrement() {
        val newCount = maxOf(0, _uiState.value.count - 1)
        _uiState.value = TallyCounterUiState(count = newCount)
        scope.launch { prefs.setTallyCount(newCount) }
    }

    fun reset() {
        _uiState.value = TallyCounterUiState(count = 0)
        scope.launch { prefs.setTallyCount(0) }
    }
}
