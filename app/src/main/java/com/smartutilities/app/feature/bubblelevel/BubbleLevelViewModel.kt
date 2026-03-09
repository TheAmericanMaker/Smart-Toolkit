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

data class BubbleLevelUiState(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isLevel: Boolean = false,
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

    init {
        if (accelerometer == null) {
            _uiState.value = BubbleLevelUiState(isAvailable = false)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        gravity = lowPass(event.values.clone(), gravity)

        val g = gravity ?: return
        val x = g[0]
        val y = g[1]
        val z = g[2]

        val pitch = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat() - 90f
        val roll = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
        val isLevel = abs(pitch) < 1.5f && abs(roll) < 1.5f

        _uiState.value = BubbleLevelUiState(
            pitch = pitch,
            roll = roll,
            isLevel = isLevel
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
