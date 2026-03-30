package com.smarttoolkit.app.feature.stopwatch

import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StopwatchStateHolder @Inject constructor(
    private val prefs: UserPreferencesRepository
) {

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var startTimeNano = 0L
    private var accumulatedMs = 0L
    private var lastLapMs = 0L
    private var restored = false

    val isRunning: Boolean get() = _uiState.value.isRunning

    init {
        scope.launch { restoreState() }
    }

    private suspend fun restoreState() {
        val savedMs = prefs.stopwatchAccumulatedMs.first()
        val savedLastLap = prefs.stopwatchLastLapMs.first()
        val lapsJson = prefs.stopwatchLapsJson.first()
        if (savedMs > 0 || lapsJson.isNotEmpty()) {
            accumulatedMs = savedMs
            lastLapMs = savedLastLap
            val laps = parseLaps(lapsJson)
            _uiState.value = StopwatchUiState(
                elapsedMs = savedMs,
                isRunning = false,
                laps = laps
            )
        }
        restored = true
    }

    fun start() {
        startTimeNano = System.nanoTime()
        _uiState.value = _uiState.value.copy(isRunning = true)
    }

    fun pause() {
        accumulatedMs += (System.nanoTime() - startTimeNano) / 1_000_000
        _uiState.value = _uiState.value.copy(isRunning = false, elapsedMs = accumulatedMs)
        persistState()
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
        persistState()
    }

    fun deleteLap(lapNumber: Int) {
        _uiState.value = _uiState.value.copy(
            laps = _uiState.value.laps.filter { it.lapNumber != lapNumber }
        )
        persistState()
    }

    fun reset() {
        accumulatedMs = 0L
        lastLapMs = 0L
        _uiState.value = StopwatchUiState()
        scope.launch { prefs.clearStopwatchState() }
    }

    private fun persistState() {
        val state = _uiState.value
        val lapsJson = serializeLaps(state.laps)
        scope.launch {
            prefs.saveStopwatchState(accumulatedMs, lastLapMs, lapsJson)
        }
    }

    private fun serializeLaps(laps: List<LapTime>): String {
        val arr = JSONArray()
        laps.forEach { lap ->
            val obj = JSONObject()
            obj.put("n", lap.lapNumber)
            obj.put("s", lap.splitMs)
            obj.put("t", lap.totalMs)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseLaps(json: String): List<LapTime> {
        if (json.isEmpty()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LapTime(obj.getInt("n"), obj.getLong("s"), obj.getLong("t"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
