package com.smarttoolkit.app.feature.timer

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var activeRingtone: Ringtone? = null

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
            _uiState.value = _uiState.value.copy(
                hours = h, minutes = m, seconds = s,
                availableSounds = sounds,
                selectedSoundIndex = savedIndex
            )
        }
    }

    private fun loadAlarmSounds(): List<AlarmSound> {
        val sounds = mutableListOf<AlarmSound>()
        // Add default alarm sound first
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        sounds.add(AlarmSound("Default Alarm", defaultUri))

        // Query available alarm ringtones
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

        // Also add notification sounds as alternatives
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
        val sounds = _uiState.value.availableSounds
        if (index in sounds.indices) {
            _uiState.value = _uiState.value.copy(selectedSoundIndex = index)
            viewModelScope.launch {
                prefs.setTimerAlarmSound(sounds[index].uri.toString())
            }
        }
    }

    fun previewSound(index: Int) {
        stopAlarm()
        val sounds = _uiState.value.availableSounds
        if (index in sounds.indices) {
            try {
                activeRingtone = RingtoneManager.getRingtone(context, sounds[index].uri)
                activeRingtone?.play()
            } catch (_: Exception) {}
        }
    }

    fun setHours(h: Int) { _uiState.value = _uiState.value.copy(hours = h.coerceIn(0, 23)) }
    fun setMinutes(m: Int) { _uiState.value = _uiState.value.copy(minutes = m.coerceIn(0, 59)) }
    fun setSeconds(s: Int) { _uiState.value = _uiState.value.copy(seconds = s.coerceIn(0, 59)) }

    fun applyPreset(totalMinutes: Int) {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        _uiState.value = _uiState.value.copy(hours = h, minutes = m, seconds = 0)
    }

    fun start() {
        val state = _uiState.value
        val totalMs = ((state.hours * 3600L) + (state.minutes * 60L) + state.seconds) * 1000L
        if (totalMs <= 0) return

        viewModelScope.launch { prefs.setTimerDuration(state.hours, state.minutes, state.seconds) }

        _uiState.value = state.copy(
            remainingMs = totalMs,
            isRunning = true,
            isConfiguring = false,
            isFinished = false
        )
        startCountdown(totalMs)
    }

    fun resume() {
        if (_uiState.value.remainingMs > 0) {
            _uiState.value = _uiState.value.copy(isRunning = true)
            startCountdown(_uiState.value.remainingMs)
        }
    }

    fun pause() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun cancel() {
        countdownJob?.cancel()
        stopAlarm()
        viewModelScope.launch {
            val h = prefs.timerHours.first()
            val m = prefs.timerMinutes.first()
            val s = prefs.timerSeconds.first()
            _uiState.value = TimerUiState(
                hours = h, minutes = m, seconds = s,
                availableSounds = _uiState.value.availableSounds,
                selectedSoundIndex = _uiState.value.selectedSoundIndex
            )
        }
    }

    fun stopAlarm() {
        try {
            activeRingtone?.stop()
        } catch (_: Exception) {}
        activeRingtone = null
    }

    fun dismissAlarm() {
        stopAlarm()
        viewModelScope.launch {
            val h = prefs.timerHours.first()
            val m = prefs.timerMinutes.first()
            val s = prefs.timerSeconds.first()
            _uiState.value = TimerUiState(
                hours = h, minutes = m, seconds = s,
                availableSounds = _uiState.value.availableSounds,
                selectedSoundIndex = _uiState.value.selectedSoundIndex
            )
        }
    }

    private fun startCountdown(totalMs: Long) {
        countdownJob?.cancel()
        val startTime = System.currentTimeMillis()
        countdownJob = viewModelScope.launch {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = totalMs - elapsed
                if (remaining <= 0) {
                    _uiState.value = _uiState.value.copy(
                        remainingMs = 0,
                        isRunning = false,
                        isFinished = true
                    )
                    playAlarm()
                    break
                }
                _uiState.value = _uiState.value.copy(remainingMs = remaining)
                delay(100)
            }
        }
    }

    private fun playAlarm() {
        try {
            val state = _uiState.value
            val uri = if (state.availableSounds.isNotEmpty() && state.selectedSoundIndex in state.availableSounds.indices) {
                state.availableSounds[state.selectedSoundIndex].uri
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            activeRingtone = RingtoneManager.getRingtone(context, uri)
            activeRingtone?.play()
        } catch (_: Exception) {}

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
    }
}
