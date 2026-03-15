package com.smarttoolkit.app.feature.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
class StopwatchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var startTimeNano = 0L
    private var accumulatedMs = 0L
    private var lastLapMs = 0L

    fun startStop() {
        if (_uiState.value.isRunning) pause() else start()
    }

    private fun start() {
        startTimeNano = System.nanoTime()
        _uiState.value = _uiState.value.copy(isRunning = true)
        tickJob = viewModelScope.launch {
            while (true) {
                val now = System.nanoTime()
                val elapsed = accumulatedMs + (now - startTimeNano) / 1_000_000
                _uiState.value = _uiState.value.copy(elapsedMs = elapsed)
                delay(16)
            }
        }
    }

    private fun pause() {
        tickJob?.cancel()
        accumulatedMs += (System.nanoTime() - startTimeNano) / 1_000_000
        _uiState.value = _uiState.value.copy(isRunning = false, elapsedMs = accumulatedMs)
    }

    fun lap() {
        if (!_uiState.value.isRunning) return
        val totalMs = _uiState.value.elapsedMs
        val splitMs = totalMs - lastLapMs
        lastLapMs = totalMs
        val lapNumber = _uiState.value.laps.size + 1
        _uiState.value = _uiState.value.copy(
            laps = _uiState.value.laps + LapTime(lapNumber, splitMs, totalMs)
        )
    }

    fun deleteLap(lapNumber: Int) {
        _uiState.value = _uiState.value.copy(
            laps = _uiState.value.laps.filter { it.lapNumber != lapNumber }
        )
    }

    fun reset() {
        tickJob?.cancel()
        accumulatedMs = 0L
        lastLapMs = 0L
        _uiState.value = StopwatchUiState()
    }
}
