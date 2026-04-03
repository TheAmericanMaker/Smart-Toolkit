package com.smarttoolkit.app.feature.soundmeter

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.sqrt

data class SoundMeterEntry(
    val timestampMs: Long,
    val db: Double
)

data class SoundMeterUiState(
    val currentDb: Double = 0.0,
    val minDb: Double = Double.MAX_VALUE,
    val maxDb: Double = 0.0,
    val avgDb: Double = 0.0,
    val calibrationOffset: Double = 0.0,
    val isRecording: Boolean = false,
    val dbHistory: List<Double> = emptyList(),
    val timestampedHistory: List<SoundMeterEntry> = emptyList()
)

@HiltViewModel
class SoundMeterViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundMeterUiState())
    val uiState: StateFlow<SoundMeterUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var dbSum: Double = 0.0
    private var sampleCount: Int = 0

    init {
        viewModelScope.launch {
            val offset = prefs.soundMeterOffset.first()
            _uiState.value = _uiState.value.copy(calibrationOffset = offset.toDouble())
        }
    }

    fun setCalibrationOffset(offset: Double) {
        _uiState.value = _uiState.value.copy(calibrationOffset = offset)
        viewModelScope.launch { prefs.setSoundMeterOffset(offset.toFloat()) }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (_uiState.value.isRecording) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            return
        }

        audioRecord?.startRecording()
        dbSum = 0.0
        sampleCount = 0
        _uiState.value = SoundMeterUiState(isRecording = true)

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (read > 0) {
                    var sum = 0.0
                    for (i in 0 until read) {
                        sum += buffer[i].toDouble() * buffer[i].toDouble()
                    }
                    val rms = sqrt(sum / read)
                    val rawDb = if (rms > 0) 20 * log10(rms / 32767.0) + 90 else 0.0
                    val clampedDb = (rawDb + _uiState.value.calibrationOffset).coerceIn(0.0, 120.0)

                    val current = _uiState.value
                    val history = (current.dbHistory + clampedDb).takeLast(100)
                    val entry = SoundMeterEntry(System.currentTimeMillis(), clampedDb)
                    val timestamped = current.timestampedHistory + entry
                    dbSum += clampedDb
                    sampleCount++
                    _uiState.value = current.copy(
                        currentDb = clampedDb,
                        minDb = minOf(current.minDb, clampedDb),
                        maxDb = maxOf(current.maxDb, clampedDb),
                        avgDb = dbSum / sampleCount,
                        dbHistory = history,
                        timestampedHistory = timestamped
                    )
                }
                delay(100)
            }
        }
    }

    fun generateExportCsv(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.appendLine("Timestamp,Decibels (dB)")
        state.timestampedHistory.forEach { entry ->
            sb.appendLine("${entry.timestampMs},%.1f".format(entry.db))
        }
        sb.appendLine()
        sb.appendLine("Summary")
        sb.appendLine("Min dB,%.1f".format(if (state.minDb == Double.MAX_VALUE) 0.0 else state.minDb))
        sb.appendLine("Max dB,%.1f".format(state.maxDb))
        sb.appendLine("Avg dB,%.1f".format(state.avgDb))
        sb.appendLine("Calibration Offset,%.1f".format(state.calibrationOffset))
        sb.appendLine("Samples,${state.timestampedHistory.size}")
        return sb.toString()
    }

    fun stopRecording() {
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}
