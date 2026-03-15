package com.smarttoolkit.app.feature.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerStateHolder @Inject constructor() {

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var startWallTime: Long = 0L
    private var totalDurationMs: Long = 0L
    private var pausedRemainingMs: Long = 0L

    fun updateConfig(hours: Int, minutes: Int, seconds: Int) {
        val current = _timerState.value
        if (current.isConfiguring) {
            _timerState.value = current.copy(hours = hours, minutes = minutes, seconds = seconds)
        }
    }

    fun updateSounds(sounds: List<AlarmSound>, selectedIndex: Int) {
        _timerState.value = _timerState.value.copy(
            availableSounds = sounds,
            selectedSoundIndex = selectedIndex
        )
    }

    fun updateSelectedSoundIndex(index: Int) {
        _timerState.value = _timerState.value.copy(selectedSoundIndex = index)
    }

    fun start(totalMs: Long) {
        totalDurationMs = totalMs
        startWallTime = System.currentTimeMillis()
        pausedRemainingMs = 0L
        _timerState.value = _timerState.value.copy(
            remainingMs = totalMs,
            isRunning = true,
            isConfiguring = false,
            isFinished = false
        )
    }

    fun pause() {
        pausedRemainingMs = _timerState.value.remainingMs
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resume() {
        if (pausedRemainingMs > 0) {
            totalDurationMs = pausedRemainingMs
            startWallTime = System.currentTimeMillis()
            pausedRemainingMs = 0L
            _timerState.value = _timerState.value.copy(isRunning = true)
        }
    }

    /** Called from the service countdown loop. Returns true if timer finished. */
    fun tick(): Boolean {
        val state = _timerState.value
        if (!state.isRunning) return false

        val elapsed = System.currentTimeMillis() - startWallTime
        val remaining = totalDurationMs - elapsed
        if (remaining <= 0) {
            _timerState.value = state.copy(
                remainingMs = 0,
                isRunning = false,
                isFinished = true
            )
            return true
        }
        _timerState.value = state.copy(remainingMs = remaining)
        return false
    }

    fun finish() {
        _timerState.value = _timerState.value.copy(
            remainingMs = 0,
            isRunning = false,
            isFinished = true
        )
    }

    fun cancel(savedHours: Int, savedMinutes: Int, savedSeconds: Int) {
        pausedRemainingMs = 0L
        _timerState.value = _timerState.value.copy(
            hours = savedHours,
            minutes = savedMinutes,
            seconds = savedSeconds,
            remainingMs = 0L,
            isRunning = false,
            isFinished = false,
            isConfiguring = true
        )
    }

    fun dismiss(savedHours: Int, savedMinutes: Int, savedSeconds: Int) {
        cancel(savedHours, savedMinutes, savedSeconds)
    }
}
