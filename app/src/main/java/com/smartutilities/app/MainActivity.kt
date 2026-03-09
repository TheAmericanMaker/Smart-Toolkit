package com.smartutilities.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.smartutilities.app.navigation.NavGraph
import com.smartutilities.app.ui.settings.SettingsViewModel
import com.smartutilities.app.ui.theme.SmartToolkitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val darkTheme = if (settingsState.useSystemTheme) systemDark else settingsState.darkMode

            SmartToolkitTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
