package com.smarttoolkit.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val useSystemTheme: Boolean = true,
    val darkMode: Boolean = false,
    val colorTheme: String = "DYNAMIC"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.useSystemTheme,
        preferencesRepository.darkMode,
        preferencesRepository.colorTheme
    ) { useSystem, dark, colorTheme ->
        SettingsUiState(useSystemTheme = useSystem, darkMode = dark, colorTheme = colorTheme)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setUseSystemTheme(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseSystemTheme(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkMode(enabled) }
    }

    fun setColorTheme(theme: String) {
        viewModelScope.launch { preferencesRepository.setColorTheme(theme) }
    }
}
