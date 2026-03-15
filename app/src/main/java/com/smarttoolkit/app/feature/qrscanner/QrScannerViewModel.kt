package com.smarttoolkit.app.feature.qrscanner

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

data class QrScannerUiState(
    val scannedValue: String? = null,
    val isUrl: Boolean = false,
    val isScanning: Boolean = true
)

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("qr_scanner")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBarcodeDetected(rawValue: String) {
        if (_uiState.value.scannedValue != null) return
        val isUrl = rawValue.startsWith("http://") || rawValue.startsWith("https://")
        _uiState.value = QrScannerUiState(
            scannedValue = rawValue,
            isUrl = isUrl,
            isScanning = false
        )
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntry(
                    featureKey = "qr_scanner",
                    label = if (isUrl) "URL: $rawValue" else rawValue,
                    value = rawValue
                )
            )
        }
    }

    fun scanAgain() {
        _uiState.value = QrScannerUiState()
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch { historyDao.delete(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearFeature("qr_scanner") }
    }
}
