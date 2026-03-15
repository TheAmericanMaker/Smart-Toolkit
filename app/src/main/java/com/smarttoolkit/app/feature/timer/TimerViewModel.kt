package com.smarttoolkit.app.feature.timer

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmSound(
    val title: String,
    val uri: Uri
)

data class TimerUiState(
    val hours: Int = 0,
    val minutes: Int = 5,
    val seconds: Int = 0,
    val remainingMs: Long = 0L,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val isConfiguring: Boolean = true,
    val availableSounds: List<AlarmSound> = emptyList(),
    val selectedSoundIndex: Int = 0
) {
    val displayTime: String
        get() {
            val totalSec = remainingMs / 1000
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            val s = totalSec % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        }
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesRepository,
    private val stateHolder: TimerStateHolder
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = stateHolder.timerState

    private var previewRingtone: Ringtone? = null

    init {
        val sounds = loadAlarmSounds()
        viewModelScope.launch {
            val h = prefs.timerHours.first()
            val m = prefs.timerMinutes.first()
            val s = prefs.timerSeconds.first()
            val savedUri = prefs.timerAlarmSound.first()
            val savedIndex = if (savedUri.isNotEmpty()) {
                sounds.indexOfFirst { it.uri.toString() == savedUri }.coerceAtLeast(0)
            } else 0
            stateHolder.updateConfig(h, m, s)
            stateHolder.updateSounds(sounds, savedIndex)
        }
    }

    private fun loadAlarmSounds(): List<AlarmSound> {
        val sounds = mutableListOf<AlarmSound>()
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        sounds.add(AlarmSound("Default Alarm", defaultUri))

        try {
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position)
                if (uri != defaultUri) {
                    sounds.add(AlarmSound(title, uri))
                }
            }
        } catch (_: Exception) {}

        try {
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_NOTIFICATION)
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position)
                sounds.add(AlarmSound(title, uri))
            }
        } catch (_: Exception) {}

        return sounds
    }

    fun selectSound(index: Int) {
        val state = uiState.value
        if (index in state.availableSounds.indices) {
            stateHolder.updateSelectedSoundIndex(index)
            viewModelScope.launch {
                prefs.setTimerAlarmSound(state.availableSounds[index].uri.toString())
            }
        }
    }

    fun previewSound(index: Int) {
        stopPreview()
        val state = uiState.value
        if (index in state.availableSounds.indices) {
            try {
                previewRingtone = RingtoneManager.getRingtone(context, state.availableSounds[index].uri)
                previewRingtone?.play()
            } catch (_: Exception) {}
        }
    }

    fun stopPreview() {
        try {
            previewRingtone?.stop()
        } catch (_: Exception) {}
        previewRingtone = null
    }

    fun setHours(h: Int) {
        val state = uiState.value
        stateHolder.updateConfig(h.coerceIn(0, 23), state.minutes, state.seconds)
    }

    fun setMinutes(m: Int) {
        val state = uiState.value
        stateHolder.updateConfig(state.hours, m.coerceIn(0, 59), state.seconds)
    }

    fun setSeconds(s: Int) {
        val state = uiState.value
        stateHolder.updateConfig(state.hours, state.minutes, s.coerceIn(0, 59))
    }

    fun applyPreset(totalMinutes: Int) {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        stateHolder.updateConfig(h, m, 0)
    }

    fun start() {
        val state = uiState.value
        val totalMs = ((state.hours * 3600L) + (state.minutes * 60L) + state.seconds) * 1000L
        if (totalMs <= 0) return

        viewModelScope.launch { prefs.setTimerDuration(state.hours, state.minutes, state.seconds) }

        // Get the alarm URI for the service
        val alarmUri = if (state.availableSounds.isNotEmpty() && state.selectedSoundIndex in state.availableSounds.indices) {
            state.availableSounds[state.selectedSoundIndex].uri.toString()
        } else ""

        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
            putExtra(TimerForegroundService.EXTRA_DURATION_MS, totalMs)
            putExtra(TimerForegroundService.EXTRA_ALARM_URI, alarmUri)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pause() {
        sendServiceAction(TimerForegroundService.ACTION_PAUSE)
    }

    fun resume() {
        sendServiceAction(TimerForegroundService.ACTION_RESUME)
    }

    fun cancel() {
        sendServiceAction(TimerForegroundService.ACTION_CANCEL)
    }

    fun dismissAlarm() {
        sendServiceAction(TimerForegroundService.ACTION_DISMISS)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            this.action = action
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {
            // Service may not be running; handle state locally
            viewModelScope.launch {
                val h = prefs.timerHours.first()
                val m = prefs.timerMinutes.first()
                val s = prefs.timerSeconds.first()
                when (action) {
                    TimerForegroundService.ACTION_CANCEL -> stateHolder.cancel(h, m, s)
                    TimerForegroundService.ACTION_DISMISS -> stateHolder.dismiss(h, m, s)
                    TimerForegroundService.ACTION_PAUSE -> stateHolder.pause()
                    TimerForegroundService.ACTION_RESUME -> stateHolder.resume()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}
