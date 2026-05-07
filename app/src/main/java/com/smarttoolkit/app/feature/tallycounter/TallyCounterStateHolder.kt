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
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class TallyCounterItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Counter",
    val count: Int = 0
)

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
            val json = prefs.tallyCountersJson.first()
            if (json.isNotEmpty()) {
                val counters = parseCounters(json)
                _uiState.value = TallyCounterUiState(counters = counters)
            } else {
                // Migrate from old single counter
                val oldCount = prefs.tallyCount.first()
                val initial = listOf(TallyCounterItem(name = "Counter 1", count = oldCount))
                _uiState.value = TallyCounterUiState(counters = initial)
                persist()
            }
        }
    }

    fun addCounter(name: String) {
        val counters = _uiState.value.counters + TallyCounterItem(name = name)
        _uiState.value = _uiState.value.copy(counters = counters)
        persist()
    }

    fun deleteCounter(id: String) {
        val counters = _uiState.value.counters.filter { it.id != id }
        if (counters.isEmpty()) return // Don't allow deleting all
        _uiState.value = _uiState.value.copy(counters = counters)
        persist()
    }

    fun increment(id: String) {
        updateCounter(id) { it.copy(count = it.count + 1) }
    }

    fun decrement(id: String) {
        updateCounter(id) { it.copy(count = maxOf(0, it.count - 1)) }
    }

    fun resetCounter(id: String) {
        updateCounter(id) { it.copy(count = 0) }
    }

    // Legacy compatibility - operates on first counter
    fun increment() { _uiState.value.counters.firstOrNull()?.let { increment(it.id) } }
    fun decrement() { _uiState.value.counters.firstOrNull()?.let { decrement(it.id) } }
    fun reset() { _uiState.value.counters.firstOrNull()?.let { resetCounter(it.id) } }

    private fun updateCounter(id: String, transform: (TallyCounterItem) -> TallyCounterItem) {
        val counters = _uiState.value.counters.map { if (it.id == id) transform(it) else it }
        _uiState.value = _uiState.value.copy(counters = counters)
        persist()
    }

    private fun persist() {
        val json = serializeCounters(_uiState.value.counters)
        scope.launch { prefs.setTallyCountersJson(json) }
    }

    private fun serializeCounters(counters: List<TallyCounterItem>): String {
        val arr = JSONArray()
        counters.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("count", c.count)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseCounters(json: String): List<TallyCounterItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                TallyCounterItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    count = obj.getInt("count")
                )
            }
        } catch (_: Exception) {
            listOf(TallyCounterItem(name = "Counter 1"))
        }
    }
}
