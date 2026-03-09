package com.smarttoolkit.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Flashlight : Screen("flashlight")
    data object Stopwatch : Screen("stopwatch")
    data object Timer : Screen("timer")
    data object Calculator : Screen("calculator")
    data object Battery : Screen("battery")
    data object Compass : Screen("compass")
    data object Network : Screen("network")
    data object Storage : Screen("storage")
    data object Ruler : Screen("ruler")
    data object QrScanner : Screen("qrscanner")
    data object UnitConverter : Screen("unitconverter")
    data object TextTools : Screen("texttools")
    data object RandomGenerator : Screen("randomgenerator")
    data object NoteList : Screen("notepad")
    data object NoteEdit : Screen("notepad/{noteId}") {
        fun createRoute(noteId: Long) = "notepad/$noteId"
        fun createNewRoute() = "notepad/-1"
    }
    data object DeviceInfo : Screen("deviceinfo")
    data object SoundMeter : Screen("soundmeter")
    data object BubbleLevel : Screen("bubblelevel")
    data object TipCalculator : Screen("tipcalculator")
    data object TallyCounter : Screen("tallycounter")
    data object MagnifyingGlass : Screen("magnifyingglass")
    data object ColorPicker : Screen("colorpicker")
}
