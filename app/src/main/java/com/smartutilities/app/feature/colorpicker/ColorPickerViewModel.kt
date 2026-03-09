package com.smartutilities.app.feature.colorpicker

import android.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ColorPickerUiState(
    val colorHex: String = "#000000",
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0,
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val lightness: Float = 0f
)

@HiltViewModel
class ColorPickerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ColorPickerUiState())
    val uiState: StateFlow<ColorPickerUiState> = _uiState.asStateFlow()

    fun onColorSampled(r: Int, g: Int, b: Int) {
        val hex = String.format("#%02X%02X%02X", r, g, b)
        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        // Convert HSV to HSL
        val h = hsv[0]
        val s = hsv[1]
        val v = hsv[2]
        val l = v * (1f - s / 2f)
        val sl = if (l == 0f || l == 1f) 0f else (v - l) / minOf(l, 1f - l)

        _uiState.value = ColorPickerUiState(
            colorHex = hex,
            red = r,
            green = g,
            blue = b,
            hue = h,
            saturation = sl,
            lightness = l
        )
    }
}
