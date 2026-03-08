package com.smartutilities.app.feature.qrscanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class QrScannerUiState(
    val scannedValue: String? = null,
    val isUrl: Boolean = false,
    val isScanning: Boolean = true
)

@HiltViewModel
class QrScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeDetected(rawValue: String) {
        if (_uiState.value.scannedValue != null) return
        _uiState.value = QrScannerUiState(
            scannedValue = rawValue,
            isUrl = rawValue.startsWith("http://") || rawValue.startsWith("https://"),
            isScanning = false
        )
    }

    fun scanAgain() {
        _uiState.value = QrScannerUiState()
    }
}
