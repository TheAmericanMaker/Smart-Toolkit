package com.smartutilities.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartutilities.app.feature.battery.BatteryScreen
import com.smartutilities.app.feature.calculator.CalculatorScreen
import com.smartutilities.app.feature.compass.CompassScreen
import com.smartutilities.app.feature.deviceinfo.DeviceInfoScreen
import com.smartutilities.app.feature.flashlight.FlashlightScreen
import com.smartutilities.app.feature.network.NetworkScreen
import com.smartutilities.app.feature.notepad.NoteEditScreen
import com.smartutilities.app.feature.notepad.NoteListScreen
import com.smartutilities.app.feature.qrscanner.QrScannerScreen
import com.smartutilities.app.feature.randomgenerator.RandomGeneratorScreen
import com.smartutilities.app.feature.ruler.RulerScreen
import com.smartutilities.app.feature.soundmeter.SoundMeterScreen
import com.smartutilities.app.feature.stopwatch.StopwatchScreen
import com.smartutilities.app.feature.storage.StorageScreen
import com.smartutilities.app.feature.texttools.TextToolsScreen
import com.smartutilities.app.feature.timer.TimerScreen
import com.smartutilities.app.feature.unitconverter.UnitConverterScreen
import com.smartutilities.app.ui.home.HomeScreen
import com.smartutilities.app.ui.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onUtilityClick = { route -> navController.navigate(route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Flashlight.route) {
            FlashlightScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Stopwatch.route) {
            StopwatchScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Timer.route) {
            TimerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Battery.route) {
            BatteryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Compass.route) {
            CompassScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Network.route) {
            NetworkScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Storage.route) {
            StorageScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Ruler.route) {
            RulerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.QrScanner.route) {
            QrScannerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UnitConverter.route) {
            UnitConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.TextTools.route) {
            TextToolsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.RandomGenerator.route) {
            RandomGeneratorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate(Screen.NoteEdit.createRoute(noteId)) },
                onNewNote = { navController.navigate(Screen.NoteEdit.createNewRoute()) }
            )
        }
        composable(
            route = Screen.NoteEdit.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteEditScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.DeviceInfo.route) {
            DeviceInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SoundMeter.route) {
            SoundMeterScreen(onBack = { navController.popBackStack() })
        }
    }
}
