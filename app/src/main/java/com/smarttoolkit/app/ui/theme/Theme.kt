package com.smarttoolkit.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SmartToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: AppColorTheme = AppColorTheme.DYNAMIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        colorTheme == AppColorTheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = colorTheme.primaryDark,
            secondary = colorTheme.secondaryDark,
            tertiary = colorTheme.tertiaryDark
        )
        else -> lightColorScheme(
            primary = colorTheme.primaryLight,
            secondary = colorTheme.secondaryLight,
            tertiary = colorTheme.tertiaryLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
