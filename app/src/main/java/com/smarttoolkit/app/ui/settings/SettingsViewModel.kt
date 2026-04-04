package com.smarttoolkit.app.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.BuildConfig
import com.smarttoolkit.app.data.billing.BillingRepository
import com.smarttoolkit.app.data.billing.BillingState
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val useSystemTheme: Boolean = true,
    val darkMode: Boolean = false,
    val colorTheme: String = "DYNAMIC",
    val adsRemoved: Boolean = false,
    val billingState: BillingState = BillingState.Idle,
    val billingAvailable: Boolean = false,
    val removeAdsPrice: String? = null,
    val appVersion: String = BuildConfig.VERSION_NAME
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.useSystemTheme,
        preferencesRepository.darkMode,
        preferencesRepository.colorTheme,
        preferencesRepository.adsRemoved,
        billingRepository.billingState,
        billingRepository.removeAdsPrice
    ) { values ->
        SettingsUiState(
            useSystemTheme = values[0] as Boolean,
            darkMode = values[1] as Boolean,
            colorTheme = values[2] as String,
            adsRemoved = values[3] as Boolean,
            billingState = values[4] as BillingState,
            billingAvailable = billingRepository.isBillingAvailable,
            removeAdsPrice = values[5] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        viewModelScope.launch {
            val owned = billingRepository.queryAndAcknowledgePurchases()
            if (owned) preferencesRepository.setAdsRemoved(true)
        }
        viewModelScope.launch {
            billingRepository.billingState.collect { state ->
                if (state is BillingState.Purchased) {
                    preferencesRepository.setAdsRemoved(true)
                }
            }
        }
    }

    fun purchaseRemoveAds(activity: Activity) {
        billingRepository.launchBillingFlow(activity)
    }

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
