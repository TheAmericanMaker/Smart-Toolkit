package com.smarttoolkit.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import com.smarttoolkit.app.feature.battery.BatteryScreen
import com.smarttoolkit.app.feature.bubblelevel.BubbleLevelScreen
import com.smarttoolkit.app.feature.colorpicker.ColorPickerScreen
import com.smarttoolkit.app.feature.magnifyingglass.MagnifyingGlassScreen
import com.smarttoolkit.app.feature.tallycounter.TallyCounterScreen
import com.smarttoolkit.app.feature.tipcalculator.TipCalculatorScreen
import com.smarttoolkit.app.feature.calculator.CalculatorScreen
import com.smarttoolkit.app.feature.compass.CompassScreen
import com.smarttoolkit.app.feature.deviceinfo.DeviceInfoScreen
import com.smarttoolkit.app.feature.flashlight.FlashlightScreen
import com.smarttoolkit.app.feature.network.NetworkScreen
import com.smarttoolkit.app.feature.notepad.NoteEditScreen
import com.smarttoolkit.app.feature.notepad.NoteListScreen
import com.smarttoolkit.app.feature.qrscanner.QrScannerScreen
import com.smarttoolkit.app.feature.randomgenerator.RandomGeneratorScreen
import com.smarttoolkit.app.feature.ruler.RulerScreen
import com.smarttoolkit.app.feature.soundmeter.SoundMeterScreen
import com.smarttoolkit.app.feature.stopwatch.StopwatchScreen
import com.smarttoolkit.app.feature.storage.StorageScreen
import com.smarttoolkit.app.feature.texttools.TextToolsScreen
import com.smarttoolkit.app.feature.timer.TimerScreen
import com.smarttoolkit.app.feature.unitconverter.UnitConverterScreen
import com.smarttoolkit.app.ui.home.HomeScreen
import com.smarttoolkit.app.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    pendingRoute: MutableStateFlow<String?> = MutableStateFlow(null)
) {
    val route by pendingRoute.collectAsState()
    LaunchedEffect(route) {
        route?.let {
            navController.navigate(it) {
                launchSingleTop = true
            }
            pendingRoute.value = null
        }
    }

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
                onNewNote = { navController.navigate(Screen.NoteEdit.createNewRoute()) },
                onNewChecklist = { navController.navigate(Screen.NoteEdit.createNewRoute("CHECKLIST")) }
            )
        }
        composable(
            route = Screen.NoteEdit.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType; defaultValue = "TEXT" }
            )
        ) {
            NoteEditScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.DeviceInfo.route) {
            DeviceInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SoundMeter.route) {
            SoundMeterScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.BubbleLevel.route) {
            BubbleLevelScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.TipCalculator.route) {
            TipCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.TallyCounter.route) {
            TallyCounterScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.MagnifyingGlass.route) {
            MagnifyingGlassScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ColorPicker.route) {
            ColorPickerScreen(onBack = { navController.popBackStack() })
        }
    }
}
