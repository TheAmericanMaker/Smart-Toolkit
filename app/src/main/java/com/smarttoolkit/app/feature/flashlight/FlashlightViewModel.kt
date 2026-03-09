package com.smarttoolkit.app.feature.flashlight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class FlashlightUiState(
    val isOn: Boolean = false,
    val isAvailable: Boolean = true
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

    fun toggle() {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, !_uiState.value.isOn)
        } catch (e: Exception) {
            // Camera in use or other error
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.unregisterTorchCallback(torchCallback)
        if (_uiState.value.isOn) {
            try {
                cameraId?.let { cameraManager.setTorchMode(it, false) }
            } catch (_: Exception) {}
        }
    }
}
