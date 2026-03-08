package com.smartutilities.app.feature.soundmeter

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.sqrt

data class SoundMeterUiState(
    val currentDb: Double = 0.0,
    val minDb: Double = Double.MAX_VALUE,
    val maxDb: Double = 0.0,
    val isRecording: Boolean = false
)

@HiltViewModel
class SoundMeterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SoundMeterUiState())
    val uiState: StateFlow<SoundMeterUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

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
                    val db = if (rms > 0) 20 * log10(rms / 32767.0) + 90 else 0.0
                    val clampedDb = db.coerceIn(0.0, 120.0)

                    val current = _uiState.value
                    _uiState.value = current.copy(
                        currentDb = clampedDb,
                        minDb = minOf(current.minDb, clampedDb),
                        maxDb = maxOf(current.maxDb, clampedDb)
                    )
                }
                delay(100)
            }
        }
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
