package com.smarttoolkit.app.feature.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class StorageVolumeInfo(
    val label: String,
    val total: Long,
    val available: Long
) {
    val used: Long get() = total - available
    val usedPercent: Float get() = if (total > 0) used.toFloat() / total else 0f
}

data class StorageUiState(
    val internalTotal: Long = 0,
    val internalAvailable: Long = 0,
    val externalVolumes: List<StorageVolumeInfo> = emptyList()
) {
    val internalUsed: Long get() = internalTotal - internalAvailable
    val internalUsedPercent: Float get() = if (internalTotal > 0) internalUsed.toFloat() / internalTotal else 0f

    // Backwards compatibility
    val externalTotal: Long get() = externalVolumes.firstOrNull()?.total ?: 0
    val externalAvailable: Long get() = externalVolumes.firstOrNull()?.available ?: 0
    val externalUsed: Long get() = externalVolumes.firstOrNull()?.used ?: 0
}

fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb)
    else "%.0f MB".format(bytes / (1024.0 * 1024))
}

@HiltViewModel
class StorageViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        // Internal storage - use data directory for actual internal storage stats
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val internalTotal = internalStat.blockSizeLong * internalStat.blockCountLong
        val internalAvail = internalStat.blockSizeLong * internalStat.availableBlocksLong

        // External/removable storage - use getExternalFilesDirs to find separate volumes
        val externalVolumes = mutableListOf<StorageVolumeInfo>()
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            // getExternalFilesDirs(null) returns paths on all shared/external storage devices.
            // Index 0 is the primary external storage (same as internal on most devices).
            // Index 1+ are truly separate volumes (SD cards, USB drives, etc).
            val externalDirs = context.getExternalFilesDirs(null)
            for (i in externalDirs.indices) {
                val dir = externalDirs[i] ?: continue
                if (i == 0) {
                    // Primary external storage is typically the same physical device as internal.
                    // Skip it to avoid showing duplicate stats.
                    continue
                }
                try {
                    val state = Environment.getExternalStorageState(dir)
                    if (state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY) {
                        val stat = StatFs(dir.path)
                        val total = stat.blockSizeLong * stat.blockCountLong
                        val avail = stat.blockSizeLong * stat.availableBlocksLong

                        // Get a label from StorageManager if possible
                        val volume: StorageVolume? = storageManager.getStorageVolume(dir)
                        val label = volume?.getDescription(context) ?: "External Storage ${externalVolumes.size + 1}"

                        externalVolumes.add(StorageVolumeInfo(label, total, avail))
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        _uiState.value = StorageUiState(internalTotal, internalAvail, externalVolumes)
    }
}
