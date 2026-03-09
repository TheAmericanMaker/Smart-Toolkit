package com.smarttoolkit.app.feature.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.sqrt

data class DeviceInfoItem(val label: String, val value: String)

data class DeviceInfoUiState(
    val items: List<DeviceInfoItem> = emptyList()
)

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceInfoUiState())
    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    init {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)

        val widthInches = dm.widthPixels / dm.xdpi
        val heightInches = dm.heightPixels / dm.ydpi
        val diagonalInches = sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())

        val totalRamGb = "%.1f GB".format(memInfo.totalMem / (1024.0 * 1024 * 1024))

        _uiState.value = DeviceInfoUiState(listOf(
            DeviceInfoItem("Model", Build.MODEL),
            DeviceInfoItem("Manufacturer", Build.MANUFACTURER),
            DeviceInfoItem("Brand", Build.BRAND),
            DeviceInfoItem("Device", Build.DEVICE),
            DeviceInfoItem("Product", Build.PRODUCT),
            DeviceInfoItem("Android Version", Build.VERSION.RELEASE),
            DeviceInfoItem("API Level", Build.VERSION.SDK_INT.toString()),
            DeviceInfoItem("Security Patch", Build.VERSION.SECURITY_PATCH),
            DeviceInfoItem("Build Number", Build.DISPLAY),
            DeviceInfoItem("Hardware", Build.HARDWARE),
            DeviceInfoItem("Board", Build.BOARD),
            DeviceInfoItem("CPU Cores", Runtime.getRuntime().availableProcessors().toString()),
            DeviceInfoItem("Total RAM", totalRamGb),
            DeviceInfoItem("Screen Resolution", "${dm.widthPixels} x ${dm.heightPixels}"),
            DeviceInfoItem("Screen Density", "${dm.densityDpi} dpi (${dm.density}x)"),
            DeviceInfoItem("Screen Size", "%.1f\"".format(diagonalInches))
        ))
    }
}
