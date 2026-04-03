package com.smarttoolkit.app.feature.flashlight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashlightStateHolder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _uiState = MutableStateFlow(FlashlightUiState())
    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var patternJob: Job? = null

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

    fun turnOff() {
        stopPattern()
        try { cameraId?.let { cameraManager.setTorchMode(it, false) } } catch (_: Exception) {}
    }

    fun setMode(mode: FlashMode) {
        val wasOn = _uiState.value.isOn
        if (wasOn) {
            stopPattern()
            try { cameraId?.let { cameraManager.setTorchMode(it, false) } } catch (_: Exception) {}
        }
        _uiState.value = _uiState.value.copy(mode = mode, isOn = false)
        if (wasOn) {
            when (mode) {
                FlashMode.STEADY -> try { cameraId?.let { cameraManager.setTorchMode(it, true) } } catch (_: Exception) {}
                FlashMode.SOS -> startSos()
                FlashMode.STROBE -> startStrobe()
            }
        }
    }

    fun setStrobeDelay(delayMs: Long) {
        _uiState.value = _uiState.value.copy(strobeDelayMs = delayMs.coerceIn(50, 500))
        // Restart strobe if currently running
        if (_uiState.value.isOn && _uiState.value.mode == FlashMode.STROBE) {
            stopPattern()
            startStrobe()
        }
    }

    private fun stopPattern() {
        patternJob?.cancel()
        patternJob = null
    }

    private fun startSos() {
        val id = cameraId ?: return
        val dit = 150L
        val dah = 450L
        val symbolGap = 150L
        val letterGap = 450L
        val wordGap = 1050L
        val pattern = listOf(
            dit, symbolGap, dit, symbolGap, dit, letterGap,
            dah, symbolGap, dah, symbolGap, dah, letterGap,
            dit, symbolGap, dit, symbolGap, dit, wordGap
        )
        patternJob = scope.launch {
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
        patternJob = scope.launch {
            _uiState.value = _uiState.value.copy(isOn = true)
            var on = false
            while (isActive) {
                on = !on
                try { cameraManager.setTorchMode(id, on) } catch (_: Exception) {}
                delay(_uiState.value.strobeDelayMs)
            }
            try { cameraManager.setTorchMode(id, false) } catch (_: Exception) {}
        }
    }
}
