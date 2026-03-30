package com.smarttoolkit.app.feature.colorpicker

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.db.HistoryDao
import com.smarttoolkit.app.data.db.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ColorPickerUiState(
    val colorHex: String = "#000000",
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0,
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val lightness: Float = 0f,
    val colorName: String = ""
)

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorPickerUiState())
    val uiState: StateFlow<ColorPickerUiState> = _uiState.asStateFlow()

    val palette: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("color_picker")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onColorSampled(r: Int, g: Int, b: Int) {
        val hex = String.format("#%02X%02X%02X", r, g, b)
        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        val h = hsv[0]
        val s = hsv[1]
        val v = hsv[2]
        val l = v * (1f - s / 2f)
        val sl = if (l == 0f || l == 1f) 0f else (v - l) / minOf(l, 1f - l)

        val colorName = ColorNameLookup.findNearest(r, g, b)
        _uiState.value = ColorPickerUiState(
            colorHex = hex,
            red = r,
            green = g,
            blue = b,
            hue = h,
            saturation = sl,
            lightness = l,
            colorName = colorName
        )
    }

    fun saveColor() {
        val state = _uiState.value
        viewModelScope.launch {
            // Avoid duplicate consecutive saves of the same color
            val existing = palette.value
            if (existing.firstOrNull()?.value == state.colorHex) return@launch
            historyDao.insert(
                HistoryEntry(
                    featureKey = "color_picker",
                    label = state.colorHex,
                    value = state.colorHex
                )
            )
        }
    }

    fun deleteColor(id: Long) {
        viewModelScope.launch { historyDao.delete(id) }
    }

    fun clearPalette() {
        viewModelScope.launch { historyDao.clearFeature("color_picker") }
    }
}
