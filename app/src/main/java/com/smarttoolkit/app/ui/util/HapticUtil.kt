package com.smarttoolkit.app.ui.util

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    }
}
