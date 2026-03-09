package com.smarttoolkit.app.feature.storage

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class StorageUiState(
    val internalTotal: Long = 0,
    val internalAvailable: Long = 0,
    val externalTotal: Long = 0,
    val externalAvailable: Long = 0
) {
    val internalUsed: Long get() = internalTotal - internalAvailable
    val externalUsed: Long get() = externalTotal - externalAvailable
    val internalUsedPercent: Float get() = if (internalTotal > 0) internalUsed.toFloat() / internalTotal else 0f
}

fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb)
    else "%.0f MB".format(bytes / (1024.0 * 1024))
}

@HiltViewModel
class StorageViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val internalTotal = internalStat.blockSizeLong * internalStat.blockCountLong
        val internalAvail = internalStat.blockSizeLong * internalStat.availableBlocksLong

        var extTotal = 0L
        var extAvail = 0L
        try {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                @Suppress("DEPRECATION")
                val extStat = StatFs(Environment.getExternalStorageDirectory().path)
                extTotal = extStat.blockSizeLong * extStat.blockCountLong
                extAvail = extStat.blockSizeLong * extStat.availableBlocksLong
            }
        } catch (_: Exception) {}

        _uiState.value = StorageUiState(internalTotal, internalAvail, extTotal, extAvail)
    }
}
