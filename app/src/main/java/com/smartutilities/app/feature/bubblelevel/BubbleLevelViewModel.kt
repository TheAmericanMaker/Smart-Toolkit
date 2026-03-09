package com.smartutilities.app.feature.bubblelevel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class BubbleLevelUiState(
    // Surface bubble (phone on its back)
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isLevel: Boolean = false,
    // Side bubble (phone on its left/right edge)
    val sideAngle: Float = 0f,
    val isSideLevel: Boolean = false,
    val isCalibrated: Boolean = false,
    val isAvailable: Boolean = true
)

@HiltViewModel
class BubbleLevelViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _uiState = MutableStateFlow(BubbleLevelUiState())
    val uiState: StateFlow<BubbleLevelUiState> = _uiState.asStateFlow()

    private var gravity: FloatArray? = null

    // Calibration offsets — subtracted from raw readings to zero out
    private var pitchOffset = 0f
    private var rollOffset = 0f
    private var sideOffset = 0f

    // Raw (pre-calibration) values, stored for calibration capture
    private var rawPitch = 0f
    private var rawRoll = 0f
    private var rawSide = 0f

    init {
        if (accelerometer == null) {
            _uiState.value = BubbleLevelUiState(isAvailable = false)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun calibrate() {
        pitchOffset = rawPitch
        rollOffset = rawRoll
        sideOffset = rawSide
        // Force an immediate UI update with zeroed values
        _uiState.value = _uiState.value.copy(
            pitch = 0f,
            roll = 0f,
            isLevel = true,
            sideAngle = 0f,
            isSideLevel = true,
            isCalibrated = true
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        gravity = lowPass(event.values.clone(), gravity)

        val g = gravity ?: return
        val x = g[0]
        val y = g[1]
        val z = g[2]

        // --- Surface (phone flat on back) ---
        // pitch: tilt forward/back, roll: tilt left/right
        rawPitch = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat() - 90f
        rawRoll = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
        val pitch = rawPitch - pitchOffset
        val roll = rawRoll - rollOffset

        // --- Side (phone on its left or right edge, landscape) ---
        // Measures tilt around the long axis when phone is on its side
        rawSide = Math.toDegrees(atan2(z.toDouble(), y.toDouble())).toFloat()
        val sideAngle = rawSide - sideOffset

        val threshold = 1.5f

        _uiState.value = BubbleLevelUiState(
            pitch = pitch,
            roll = roll,
            isLevel = abs(pitch) < threshold && abs(roll) < threshold,
            sideAngle = sideAngle,
            isSideLevel = abs(sideAngle) < threshold,
            isCalibrated = _uiState.value.isCalibrated
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        val alpha = 0.1f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
