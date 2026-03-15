package com.smarttoolkit.app.feature.flashlight

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class FlashMode { STEADY, SOS, STROBE }

data class FlashlightUiState(
    val isOn: Boolean = false,
    val isAvailable: Boolean = true,
    val mode: FlashMode = FlashMode.STEADY
)

@HiltViewModel
class FlashlightViewModel @Inject constructor(
    private val stateHolder: FlashlightStateHolder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<FlashlightUiState> = stateHolder.uiState

    fun toggle() {
        stateHolder.toggle()
        val state = stateHolder.uiState.value
        if (state.isOn) {
            val intent = Intent(context, FlashlightForegroundService::class.java).apply {
                action = FlashlightForegroundService.ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        } else {
            val intent = Intent(context, FlashlightForegroundService::class.java).apply {
                action = FlashlightForegroundService.ACTION_STOP
            }
            try { context.startService(intent) } catch (_: Exception) {}
        }
    }

    fun setMode(mode: FlashMode) {
        val wasOn = stateHolder.uiState.value.isOn
        stateHolder.setMode(mode)
        if (wasOn && stateHolder.uiState.value.isOn) {
            // Service is running, it will pick up the state change via observation
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't turn off flashlight when leaving screen — service keeps it alive
    }
}
