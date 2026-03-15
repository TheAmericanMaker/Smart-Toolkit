package com.smarttoolkit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.smarttoolkit.app.navigation.NavGraph
import com.smarttoolkit.app.ui.settings.SettingsViewModel
import com.smarttoolkit.app.ui.theme.AppColorTheme
import com.smarttoolkit.app.ui.theme.SmartToolkitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val pendingNavigationRoute = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNavigationIntent(intent)
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
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        val route = intent?.getStringExtra("navigate_to")
        if (route != null) {
            pendingNavigationRoute.value = route
        }
    }
}
