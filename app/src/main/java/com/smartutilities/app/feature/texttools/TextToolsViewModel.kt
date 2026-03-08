package com.smartutilities.app.feature.texttools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TextToolsUiState(
    val text: String = "",
    val copied: Boolean = false
) {
    val charCount: Int get() = text.length
    val wordCount: Int get() = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val lineCount: Int get() = if (text.isEmpty()) 0 else text.lines().size
    val sentenceCount: Int get() = if (text.isBlank()) 0 else text.split("[.!?]+".toRegex()).count { it.isNotBlank() }
}

@HiltViewModel
class TextToolsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextToolsUiState())
    val uiState: StateFlow<TextToolsUiState> = _uiState.asStateFlow()

    fun onTextChange(text: String) {
        _uiState.value = _uiState.value.copy(text = text, copied = false)
    }

    fun toUpperCase() { _uiState.value = _uiState.value.copy(text = _uiState.value.text.uppercase()) }
    fun toLowerCase() { _uiState.value = _uiState.value.copy(text = _uiState.value.text.lowercase()) }

    fun toTitleCase() {
        _uiState.value = _uiState.value.copy(
            text = _uiState.value.text.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        )
    }

    fun reverse() { _uiState.value = _uiState.value.copy(text = _uiState.value.text.reversed()) }

    fun removeExtraSpaces() {
        _uiState.value = _uiState.value.copy(text = _uiState.value.text.trim().replace("\\s+".toRegex(), " "))
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("text", _uiState.value.text))
        _uiState.value = _uiState.value.copy(copied = true)
    }

    fun clear() { _uiState.value = TextToolsUiState() }
}
