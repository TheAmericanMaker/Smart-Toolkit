package com.smartutilities.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.ui.graphics.vector.ImageVector

data class UtilityItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val route: String,
    val description: String
)

val allUtilities = listOf(
    UtilityItem("flashlight", "Flashlight", Icons.Filled.FlashOn, "flashlight", "Toggle device flashlight"),
    UtilityItem("stopwatch", "Stopwatch", Icons.Filled.WatchLater, "stopwatch", "Precision stopwatch with laps"),
    UtilityItem("timer", "Timer", Icons.Filled.Timer, "timer", "Countdown timer with alarm"),
    UtilityItem("calculator", "Calculator", Icons.Filled.Calculate, "calculator", "Basic & scientific calculator"),
    UtilityItem("battery", "Battery", Icons.Filled.BatteryFull, "battery", "Battery status & info"),
    UtilityItem("compass", "Compass", Icons.Filled.Explore, "compass", "Digital compass"),
    UtilityItem("network", "Network", Icons.Filled.NetworkCheck, "network", "Network information"),
    UtilityItem("storage", "Storage", Icons.Filled.SdStorage, "storage", "Storage analyzer"),
    UtilityItem("ruler", "Ruler", Icons.Filled.Straighten, "ruler", "Screen ruler"),
    UtilityItem("qrscanner", "QR Scanner", Icons.Filled.QrCodeScanner, "qrscanner", "Scan QR & barcodes"),
    UtilityItem("unitconverter", "Unit Converter", Icons.Filled.CompareArrows, "unitconverter", "Convert between units"),
    UtilityItem("texttools", "Text Tools", Icons.Filled.TextFields, "texttools", "Text manipulation tools"),
    UtilityItem("randomgenerator", "Random", Icons.Filled.Casino, "randomgenerator", "Random number generator"),
    UtilityItem("notepad", "Notepad", Icons.Filled.EditNote, "notepad", "Simple note-taking"),
    UtilityItem("deviceinfo", "Device Info", Icons.Filled.DeviceHub, "deviceinfo", "Device information"),
    UtilityItem("soundmeter", "Sound Meter", Icons.Filled.GraphicEq, "soundmeter", "Measure ambient sound")
)
