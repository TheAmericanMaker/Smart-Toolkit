package com.smarttoolkit.app.feature.stopwatch

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class LapTime(val lapNumber: Int, val splitMs: Long, val totalMs: Long)

data class StopwatchUiState(
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<LapTime> = emptyList()
) {
    val displayTime: String get() = formatTime(elapsedMs)
}

fun formatTime(ms: Long): String {
    val minutes = (ms / 60000) % 60
    val seconds = (ms / 1000) % 60
    val centis = (ms / 10) % 100
    val hours = ms / 3600000
    return if (hours > 0) "%d:%02d:%02d.%02d".format(hours, minutes, seconds, centis)
    else "%02d:%02d.%02d".format(minutes, seconds, centis)
}

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    private val stateHolder: StopwatchStateHolder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<StopwatchUiState> = stateHolder.uiState

    fun startStop() {
        if (uiState.value.isRunning) {
            sendServiceAction(StopwatchForegroundService.ACTION_PAUSE)
        } else {
            if (uiState.value.elapsedMs > 0L) {
                sendServiceAction(StopwatchForegroundService.ACTION_RESUME)
            } else {
                sendServiceAction(StopwatchForegroundService.ACTION_START)
            }
        }
    }

    fun lap() {
        sendServiceAction(StopwatchForegroundService.ACTION_LAP)
    }

    fun deleteLap(lapNumber: Int) {
        stateHolder.deleteLap(lapNumber)
    }

    fun reset() {
        sendServiceAction(StopwatchForegroundService.ACTION_STOP)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(context, StopwatchForegroundService::class.java).apply {
            this.action = action
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
