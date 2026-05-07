package com.smarttoolkit.app.ui.theme

import androidx.compose.ui.graphics.Color

// --- Default (Blue) ---
val Blue80 = Color(0xFFBBDEFB)
val BlueGrey80 = Color(0xFFB0BEC5)
val Teal80 = Color(0xFF80CBC4)

val Blue40 = Color(0xFF1976D2)
val BlueGrey40 = Color(0xFF546E7A)
val Teal40 = Color(0xFF00897B)

// --- Preset color themes ---
// Each theme defines primary, secondary, tertiary for light and dark

enum class AppColorTheme(
    val label: String,
    val previewColor: Color,
    val primaryLight: Color,
    val secondaryLight: Color,
    val tertiaryLight: Color,
    val primaryDark: Color,
    val secondaryDark: Color,
    val tertiaryDark: Color
) {
    DYNAMIC(
        label = "Dynamic",
        previewColor = Color(0xFF6750A4), // Material You purple placeholder
        primaryLight = Blue40, secondaryLight = BlueGrey40, tertiaryLight = Teal40,
        primaryDark = Blue80, secondaryDark = BlueGrey80, tertiaryDark = Teal80
    ),
    BLUE(
        label = "Blue",
        previewColor = Color(0xFF1976D2),
        primaryLight = Color(0xFF1976D2), secondaryLight = Color(0xFF546E7A), tertiaryLight = Color(0xFF00897B),
        primaryDark = Color(0xFFBBDEFB), secondaryDark = Color(0xFFB0BEC5), tertiaryDark = Color(0xFF80CBC4)
    ),
    RED(
        label = "Red",
        previewColor = Color(0xFFD32F2F),
        primaryLight = Color(0xFFD32F2F), secondaryLight = Color(0xFF616161), tertiaryLight = Color(0xFFFF6F00),
        primaryDark = Color(0xFFEF9A9A), secondaryDark = Color(0xFFBDBDBD), tertiaryDark = Color(0xFFFFCC80)
    ),
    GREEN(
        label = "Green",
        previewColor = Color(0xFF388E3C),
        primaryLight = Color(0xFF388E3C), secondaryLight = Color(0xFF5D4037), tertiaryLight = Color(0xFF00796B),
        primaryDark = Color(0xFFA5D6A7), secondaryDark = Color(0xFFBCAAA4), tertiaryDark = Color(0xFF80CBC4)
    ),
    PURPLE(
        label = "Purple",
        previewColor = Color(0xFF7B1FA2),
        primaryLight = Color(0xFF7B1FA2), secondaryLight = Color(0xFF455A64), tertiaryLight = Color(0xFFC2185B),
        primaryDark = Color(0xFFCE93D8), secondaryDark = Color(0xFFB0BEC5), tertiaryDark = Color(0xFFF48FB1)
    ),
    ORANGE(
        label = "Orange",
        previewColor = Color(0xFFE65100),
        primaryLight = Color(0xFFE65100), secondaryLight = Color(0xFF4E342E), tertiaryLight = Color(0xFFF9A825),
        primaryDark = Color(0xFFFFCC80), secondaryDark = Color(0xFFBCAAA4), tertiaryDark = Color(0xFFFFF176)
    ),
    PINK(
        label = "Pink",
        previewColor = Color(0xFFC2185B),
        primaryLight = Color(0xFFC2185B), secondaryLight = Color(0xFF5D4037), tertiaryLight = Color(0xFF7B1FA2),
        primaryDark = Color(0xFFF48FB1), secondaryDark = Color(0xFFBCAAA4), tertiaryDark = Color(0xFFCE93D8)
    ),
    GOLD(
        label = "Gold",
        previewColor = Color(0xFFF9A825),
        primaryLight = Color(0xFFF9A825), secondaryLight = Color(0xFF5D4037), tertiaryLight = Color(0xFFE65100),
        primaryDark = Color(0xFFFFF176), secondaryDark = Color(0xFFBCAAA4), tertiaryDark = Color(0xFFFFCC80)
    );

    companion object {
        fun fromName(name: String): AppColorTheme =
            entries.firstOrNull { it.name == name } ?: DYNAMIC
    }
}
