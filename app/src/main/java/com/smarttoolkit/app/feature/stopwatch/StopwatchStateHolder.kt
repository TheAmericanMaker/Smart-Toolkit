package com.smarttoolkit.app.feature.stopwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StopwatchStateHolder @Inject constructor() {

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private var startTimeNano = 0L
    private var accumulatedMs = 0L
    private var lastLapMs = 0L

    val isRunning: Boolean get() = _uiState.value.isRunning

    fun start() {
        startTimeNano = System.nanoTime()
        _uiState.value = _uiState.value.copy(isRunning = true)
    }

    fun pause() {
        accumulatedMs += (System.nanoTime() - startTimeNano) / 1_000_000
        _uiState.value = _uiState.value.copy(isRunning = false, elapsedMs = accumulatedMs)
    }

    fun tick() {
        if (!_uiState.value.isRunning) return
        val now = System.nanoTime()
        val elapsed = accumulatedMs + (now - startTimeNano) / 1_000_000
        _uiState.value = _uiState.value.copy(elapsedMs = elapsed)
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
        accumulatedMs = 0L
        lastLapMs = 0L
        _uiState.value = StopwatchUiState()
    }
}
