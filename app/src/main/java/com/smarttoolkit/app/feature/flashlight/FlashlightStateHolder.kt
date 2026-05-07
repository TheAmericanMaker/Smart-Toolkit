package com.smarttoolkit.app.feature.flashlight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class FlashlightStateHolder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _uiState = MutableStateFlow(FlashlightUiState())
    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val controlMutex = Mutex()
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var patternJob: Job? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(camId: String, enabled: Boolean) {
            if (
                camId == cameraId &&
                !enabled &&
                patternJob == null &&
                _uiState.value.isOn &&
                _uiState.value.mode == FlashMode.STEADY
            ) {
                _uiState.value = _uiState.value.copy(isOn = false)
            }
        }
    }

    init {
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
        _uiState.value = _uiState.value.copy(isAvailable = cameraId != null)
        cameraManager.registerTorchCallback(torchCallback, null)
    }

    fun toggle() {
        if (cameraId == null) return
        scope.launch {
            controlMutex.withLock {
                if (_uiState.value.isOn) {
                    turnOffLocked()
                } else {
                    _uiState.value = _uiState.value.copy(isOn = true)
                    startActiveModeLocked()
                }
            }
        }
    }

    fun turnOff() {
        if (cameraId == null) return
        scope.launch {
            controlMutex.withLock {
                turnOffLocked()
            }
        }
    }

    fun setMode(mode: FlashMode) {
        if (cameraId == null) return
        scope.launch {
            controlMutex.withLock {
                if (_uiState.value.mode == mode) return@withLock

                val shouldKeepRunning = _uiState.value.isOn
                cancelPatternLocked()
                setTorchEnabled(false)
                _uiState.value = _uiState.value.copy(mode = mode, isOn = shouldKeepRunning)

                if (shouldKeepRunning) {
                    startActiveModeLocked()
                }
            }
        }
    }

    fun setStrobeDelay(delayMs: Long) {
        if (cameraId == null) return
        scope.launch {
            controlMutex.withLock {
                _uiState.value = _uiState.value.copy(strobeDelayMs = delayMs.coerceIn(50, 500))
                if (_uiState.value.isOn && _uiState.value.mode == FlashMode.STROBE) {
                    cancelPatternLocked()
                    setTorchEnabled(false)
                    startStrobeLocked()
                }
            }
        }
    }

    private suspend fun turnOffLocked() {
        _uiState.value = _uiState.value.copy(isOn = false)
        cancelPatternLocked()
        setTorchEnabled(false)
    }

    private suspend fun cancelPatternLocked() {
        val job = patternJob
        patternJob = null
        job?.cancelAndJoin()
    }

    private suspend fun startActiveModeLocked() {
        when (_uiState.value.mode) {
            FlashMode.STEADY -> setTorchEnabled(true)
            FlashMode.SOS -> startSosLocked()
            FlashMode.STROBE -> startStrobeLocked()
        }
    }

    private fun setTorchEnabled(enabled: Boolean) {
        try {
            cameraId?.let { cameraManager.setTorchMode(it, enabled) }
        } catch (_: Exception) {
        }
    }

    private fun startSosLocked() {
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
            while (isActive && _uiState.value.isOn && _uiState.value.mode == FlashMode.SOS) {
                for (index in pattern.indices) {
                    if (!isActive || !_uiState.value.isOn || _uiState.value.mode != FlashMode.SOS) {
                        break
                    }
                    val isFlashOn = index % 2 == 0
                    try {
                        cameraManager.setTorchMode(id, isFlashOn)
                    } catch (_: Exception) {
                    }
                    delay(pattern[index])
                }
            }
            try {
                cameraManager.setTorchMode(id, false)
            } catch (_: Exception) {
            }
        }
    }

    private fun startStrobeLocked() {
        val id = cameraId ?: return
        patternJob = scope.launch {
            var on = false
            while (isActive && _uiState.value.isOn && _uiState.value.mode == FlashMode.STROBE) {
                on = !on
                try {
                    cameraManager.setTorchMode(id, on)
                } catch (_: Exception) {
                }
                delay(_uiState.value.strobeDelayMs)
            }
            try {
                cameraManager.setTorchMode(id, false)
            } catch (_: Exception) {
            }
        }
    }
}
