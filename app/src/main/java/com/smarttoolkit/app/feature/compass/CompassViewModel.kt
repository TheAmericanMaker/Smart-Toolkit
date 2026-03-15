package com.smarttoolkit.app.feature.compass

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

data class CompassUiState(
    val azimuth: Float = 0f,
    val isAvailable: Boolean = true,
    val accuracy: Int = -1,
    val lockedBearing: Float? = null
) {
    val degrees: Int get() = ((azimuth + 360) % 360).toInt()
    val direction: String
        get() {
            val d = degrees
            return when {
                d < 23 -> "N"
                d < 68 -> "NE"
                d < 113 -> "E"
                d < 158 -> "SE"
                d < 203 -> "S"
                d < 248 -> "SW"
                d < 293 -> "W"
                d < 338 -> "NW"
                else -> "N"
            }
        }
}

@HiltViewModel
class CompassViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var currentAzimuth = 0f

    init {
        if (accelerometer == null || magnetometer == null) {
            _uiState.value = CompassUiState(isAvailable = false)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = lowPass(event.values.clone(), gravity)
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = lowPass(event.values.clone(), geomagnetic)
        }

        val g = gravity ?: return
        val m = geomagnetic ?: return

        val r = FloatArray(9)
        val i = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, i, g, m)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            currentAzimuth = currentAzimuth + 0.15f * (azimuthDeg - currentAzimuth)
            _uiState.value = _uiState.value.copy(azimuth = currentAzimuth)
        }
    }

    fun toggleLockBearing() {
        val current = _uiState.value
        _uiState.value = if (current.lockedBearing == null) {
            current.copy(lockedBearing = (current.azimuth + 360) % 360)
        } else {
            current.copy(lockedBearing = null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _uiState.value = _uiState.value.copy(accuracy = acc)
        }
    }

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
