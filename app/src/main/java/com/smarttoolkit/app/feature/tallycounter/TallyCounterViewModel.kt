package com.smarttoolkit.app.feature.tallycounter

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class TallyCounterUiState(
    val counters: List<TallyCounterItem> = listOf(TallyCounterItem(name = "Counter 1")),
    val count: Int = 0 // Legacy, computed from first counter
) {
    val totalCount: Int get() = counters.firstOrNull()?.count ?: 0
}

@HiltViewModel
class TallyCounterViewModel @Inject constructor(
    private val stateHolder: TallyCounterStateHolder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<TallyCounterUiState> = stateHolder.uiState

    var isNotificationActive by mutableStateOf(false)
        private set

    init {
        stateHolder.ensureLoaded()
    }

    fun increment() {
        stateHolder.increment()
    }

    fun decrement() {
        stateHolder.decrement()
    }

    fun reset() {
        stateHolder.reset()
    }

    fun addCounter(name: String) { stateHolder.addCounter(name) }
    fun deleteCounter(id: String) { stateHolder.deleteCounter(id) }
    fun incrementCounter(id: String) { stateHolder.increment(id) }
    fun decrementCounter(id: String) { stateHolder.decrement(id) }
    fun resetCounter(id: String) { stateHolder.resetCounter(id) }

    fun startNotificationService() {
        if (isNotificationActive) return
        isNotificationActive = true
        val intent = Intent(context, TallyCounterForegroundService::class.java).apply {
            action = TallyCounterForegroundService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopNotificationService() {
        if (!isNotificationActive) return
        isNotificationActive = false
        val intent = Intent(context, TallyCounterForegroundService::class.java).apply {
            action = TallyCounterForegroundService.ACTION_STOP
        }
        try { context.startService(intent) } catch (_: Exception) {}
    }
}
