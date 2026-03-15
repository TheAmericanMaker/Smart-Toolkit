package com.smarttoolkit.app.feature.flashlight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FlashMode { STEADY, SOS, STROBE }

data class FlashlightUiState(
    val isOn: Boolean = false,
    val isAvailable: Boolean = true,
    val mode: FlashMode = FlashMode.STEADY
)

@HiltViewModel
class FlashlightViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashlightUiState())
    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(camId: String, enabled: Boolean) {
            if (camId == cameraId) {
                _uiState.value = _uiState.value.copy(isOn = enabled)
            }
        }
    }

    init {
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            null
        }
        _uiState.value = _uiState.value.copy(isAvailable = cameraId != null)
        cameraManager.registerTorchCallback(torchCallback, null)
    }

    private var patternJob: Job? = null

    fun toggle() {
        val id = cameraId ?: return
        if (_uiState.value.isOn) {
            stopPattern()
            try { cameraManager.setTorchMode(id, false) } catch (_: Exception) {}
        } else {
            when (_uiState.value.mode) {
                FlashMode.STEADY -> try { cameraManager.setTorchMode(id, true) } catch (_: Exception) {}
                FlashMode.SOS -> startSos()
                FlashMode.STROBE -> startStrobe()
            }
        }
    }

    fun setMode(mode: FlashMode) {
        val wasOn = _uiState.value.isOn
        if (wasOn) {
            stopPattern()
            try { cameraId?.let { cameraManager.setTorchMode(it, false) } } catch (_: Exception) {}
        }
        _uiState.value = _uiState.value.copy(mode = mode, isOn = false)
        if (wasOn) {
            // Restart with new mode
            when (mode) {
                FlashMode.STEADY -> try { cameraId?.let { cameraManager.setTorchMode(it, true) } } catch (_: Exception) {}
                FlashMode.SOS -> startSos()
                FlashMode.STROBE -> startStrobe()
            }
        }
    }

    private fun stopPattern() {
        patternJob?.cancel()
        patternJob = null
    }

    private fun startSos() {
        val id = cameraId ?: return
        // SOS: ... --- ... (dit dit dit dah dah dah dit dit dit)
        val dit = 150L
        val dah = 450L
        val symbolGap = 150L
        val letterGap = 450L
        val wordGap = 1050L
        val pattern = listOf(
            dit, symbolGap, dit, symbolGap, dit, letterGap,  // S
            dah, symbolGap, dah, symbolGap, dah, letterGap,  // O
            dit, symbolGap, dit, symbolGap, dit, wordGap      // S
        )
        patternJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOn = true)
            while (isActive) {
                for (i in pattern.indices) {
                    if (!isActive) break
                    val isFlashOn = i % 2 == 0
                    try { cameraManager.setTorchMode(id, isFlashOn) } catch (_: Exception) {}
                    delay(pattern[i])
                }
            }
            try { cameraManager.setTorchMode(id, false) } catch (_: Exception) {}
        }
    }

    private fun startStrobe() {
        val id = cameraId ?: return
        patternJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOn = true)
            var on = false
            while (isActive) {
                on = !on
                try { cameraManager.setTorchMode(id, on) } catch (_: Exception) {}
                delay(100L)
            }
            try { cameraManager.setTorchMode(id, false) } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPattern()
        cameraManager.unregisterTorchCallback(torchCallback)
        try {
            cameraId?.let { cameraManager.setTorchMode(it, false) }
        } catch (_: Exception) {}
    }
}
