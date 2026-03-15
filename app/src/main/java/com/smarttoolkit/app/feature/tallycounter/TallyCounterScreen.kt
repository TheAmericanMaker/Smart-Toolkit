package com.smarttoolkit.app.feature.tallycounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.RequestNotificationPermission
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun TallyCounterScreen(
    onBack: () -> Unit,
    viewModel: TallyCounterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showResetDialog by remember { mutableStateOf(false) }

    RequestNotificationPermission()

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Counter") },
            text = { Text("Reset the counter to 0?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reset()
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Tally Counter",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (viewModel.isNotificationActive) {
                                viewModel.stopNotificationService()
                            } else {
                                viewModel.startNotificationService()
                            }
                        }
                    ) {
                        Icon(
                            if (viewModel.isNotificationActive) Icons.Filled.Notifications
                            else Icons.Filled.NotificationsOff,
                            contentDescription = if (viewModel.isNotificationActive) "Unpin from notifications"
                            else "Pin to notifications"
                        )
                    }
                    IconButton(
                        onClick = { showResetDialog = true },
                        enabled = state.count > 0
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.count.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 96.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.decrement()
                    },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrement", modifier = Modifier.size(32.dp))
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.increment()
                    },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increment", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
