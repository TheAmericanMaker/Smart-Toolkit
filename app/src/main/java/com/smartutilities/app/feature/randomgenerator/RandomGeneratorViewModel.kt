package com.smartutilities.app.feature.randomgenerator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

enum class RandomMode { NUMBER, DICE, COIN, PASSWORD }

data class RandomGeneratorUiState(
    val mode: RandomMode = RandomMode.NUMBER,
    val min: String = "1",
    val max: String = "100",
    val result: String = "",
    val passwordLength: String = "16",
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeDigits: Boolean = true,
    val includeSymbols: Boolean = false
)

@HiltViewModel
class RandomGeneratorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RandomGeneratorUiState())
    val uiState: StateFlow<RandomGeneratorUiState> = _uiState.asStateFlow()

    fun setMode(mode: RandomMode) { _uiState.value = _uiState.value.copy(mode = mode, result = "") }
    fun setMin(v: String) { _uiState.value = _uiState.value.copy(min = v) }
    fun setMax(v: String) { _uiState.value = _uiState.value.copy(max = v) }
    fun setPasswordLength(v: String) { _uiState.value = _uiState.value.copy(passwordLength = v) }
    fun toggleUppercase() { _uiState.value = _uiState.value.copy(includeUppercase = !_uiState.value.includeUppercase) }
    fun toggleLowercase() { _uiState.value = _uiState.value.copy(includeLowercase = !_uiState.value.includeLowercase) }
    fun toggleDigits() { _uiState.value = _uiState.value.copy(includeDigits = !_uiState.value.includeDigits) }
    fun toggleSymbols() { _uiState.value = _uiState.value.copy(includeSymbols = !_uiState.value.includeSymbols) }

    fun generate() {
        val s = _uiState.value
        val result = when (s.mode) {
            RandomMode.NUMBER -> {
                val min = s.min.toLongOrNull() ?: 0
                val max = s.max.toLongOrNull() ?: 100
                if (min > max) "Min must be ≤ Max"
                else Random.nextLong(min, max + 1).toString()
            }
            RandomMode.DICE -> Random.nextInt(1, 7).toString()
            RandomMode.COIN -> if (Random.nextBoolean()) "Heads" else "Tails"
            RandomMode.PASSWORD -> {
                val length = s.passwordLength.toIntOrNull()?.coerceIn(4, 128) ?: 16
                val chars = buildString {
                    if (s.includeUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                    if (s.includeLowercase) append("abcdefghijklmnopqrstuvwxyz")
                    if (s.includeDigits) append("0123456789")
                    if (s.includeSymbols) append("!@#\$%^&*()-_=+[]{}|;:,.<>?")
                }
                if (chars.isEmpty()) "Select at least one character type"
                else (1..length).map { chars.random() }.joinToString("")
            }
        }
        _uiState.value = s.copy(result = result)
    }
}
