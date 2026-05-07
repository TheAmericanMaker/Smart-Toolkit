package com.smarttoolkit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import com.smarttoolkit.app.navigation.NavGraph
import com.smarttoolkit.app.ui.settings.SettingsViewModel
import com.smarttoolkit.app.ui.theme.AppColorTheme
import com.smarttoolkit.app.ui.theme.SmartToolkitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    val pendingNavigationRoute = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handledIntentRoute = handleNavigationIntent(intent)
        if (savedInstanceState == null && !handledIntentRoute) {
            lifecycleScope.launch {
                val lastRoute = preferencesRepository.lastActiveRoute.first()
                if (!lastRoute.isNullOrBlank() && pendingNavigationRoute.value == null) {
                    pendingNavigationRoute.value = lastRoute
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val darkTheme = if (settingsState.useSystemTheme) systemDark else settingsState.darkMode
            val colorTheme = AppColorTheme.fromName(settingsState.colorTheme)

            SmartToolkitTheme(darkTheme = darkTheme, colorTheme = colorTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController, pendingRoute = pendingNavigationRoute)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?): Boolean {
        val route = intent?.getStringExtra("navigate_to")
        return if (!route.isNullOrBlank()) {
            pendingNavigationRoute.value = route
            true
        } else {
            false
        }
    }
}
