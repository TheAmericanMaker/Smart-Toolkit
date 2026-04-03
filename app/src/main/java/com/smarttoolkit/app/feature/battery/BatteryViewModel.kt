package com.smarttoolkit.app.feature.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.db.HistoryDao
import com.smarttoolkit.app.data.db.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class BatteryUiState(
    val level: Int = 0,
    val scale: Int = 100,
    val status: String = "Unknown",
    val plugged: String = "Not plugged",
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val showFahrenheit: Boolean = false,
    val estimatedTimeRemaining: String = ""
) {
    val percentage: Int get() = if (scale > 0) (level * 100 / scale) else 0
    val temperatureDisplay: String get() =
        if (showFahrenheit) "%.1f\u00B0F".format(temperature * 9f / 5f + 32f)
        else "%.1f\u00B0C".format(temperature)
}

@HiltViewModel
class BatteryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("battery")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastPercent: Int = -1
    private var lastTimestamp: Long = 0L
    private var lastPlugged: String = ""

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateFromIntent(intent)
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(receiver, filter)
        intent?.let { updateFromIntent(it) }
    }

    private fun updateFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val percent = if (scale > 0) (level * 100 / scale) else 0
        val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }
        val plugged = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not plugged"
        }

        // Log plugged state changes
        if (lastPlugged.isNotEmpty() && plugged != lastPlugged) {
            val label = if (plugged != "Not plugged") "Charging via $plugged at $percent%" else "Unplugged at $percent%"
            viewModelScope.launch {
                historyDao.insert(HistoryEntry(featureKey = "battery", label = label, value = "$percent"))
            }
        }
        lastPlugged = plugged

        // Estimate time remaining
        val now = System.currentTimeMillis()
        var estimate = ""
        if (lastPercent >= 0 && lastTimestamp > 0 && percent != lastPercent) {
            val elapsedHours = (now - lastTimestamp) / 3600000.0
            if (elapsedHours > 0.005) { // at least ~18 seconds
                val ratePerHour = abs(percent - lastPercent) / elapsedHours
                if (ratePerHour > 0.1) {
                    val hoursLeft = if (status == "Charging") {
                        (100 - percent) / ratePerHour
                    } else {
                        percent / ratePerHour
                    }
                    val h = hoursLeft.toInt()
                    val m = ((hoursLeft - h) * 60).toInt()
                    estimate = if (status == "Charging") "~${h}h ${m}m to full" else "~${h}h ${m}m remaining"
                }
            }
        }
        if (percent != lastPercent) {
            lastPercent = percent
            lastTimestamp = now
        }

        _uiState.value = _uiState.value.copy(
            level = level,
            scale = scale,
            status = status,
            plugged = plugged,
            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f,
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0),
            health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            },
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            estimatedTimeRemaining = estimate.ifEmpty { _uiState.value.estimatedTimeRemaining }
        )
    }

    fun toggleTemperatureUnit() {
        _uiState.value = _uiState.value.copy(showFahrenheit = !_uiState.value.showFahrenheit)
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearFeature("battery") }
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }
}
