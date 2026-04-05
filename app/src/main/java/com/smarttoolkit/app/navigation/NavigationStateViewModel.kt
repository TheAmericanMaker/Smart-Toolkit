package com.smarttoolkit.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationStateViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private var lastPersistedRoute: String? = null

    fun onDestinationChanged(route: String?) {
        if (route == lastPersistedRoute) return
        lastPersistedRoute = route
        viewModelScope.launch {
            preferencesRepository.setLastActiveRoute(route)
        }
    }
}
