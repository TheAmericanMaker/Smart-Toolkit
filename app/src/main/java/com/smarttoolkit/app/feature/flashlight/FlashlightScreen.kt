package com.smarttoolkit.app.feature.flashlight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.RequestNotificationPermission
import com.smarttoolkit.app.ui.components.UtilityTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashlightScreen(
    onBack: () -> Unit,
    viewModel: FlashlightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RequestNotificationPermission()

    Scaffold(
        topBar = { UtilityTopBar(title = "Flashlight", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!state.isAvailable) {
                Text("Flash not available on this device", style = MaterialTheme.typography.bodyLarge)
            } else {
                // Mode selection
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    FilterChip(
                        selected = state.mode == FlashMode.STEADY,
                        onClick = { viewModel.setMode(FlashMode.STEADY) },
                        label = { Text("Steady") }
                    )
                    FilterChip(
                        selected = state.mode == FlashMode.SOS,
                        onClick = { viewModel.setMode(FlashMode.SOS) },
                        label = { Text("SOS") }
                    )
                    FilterChip(
                        selected = state.mode == FlashMode.STROBE,
                        onClick = { viewModel.setMode(FlashMode.STROBE) },
                        label = { Text("Strobe") }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                FilledIconToggleButton(
                    checked = state.isOn,
                    onCheckedChange = { viewModel.toggle() },
                    modifier = Modifier.size(120.dp)
                ) {
                    Icon(
                        imageVector = if (state.isOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Toggle flashlight",
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when {
                        !state.isOn -> "OFF"
                        state.mode == FlashMode.SOS -> "SOS"
                        state.mode == FlashMode.STROBE -> "STROBE"
                        else -> "ON"
                    },
                    style = MaterialTheme.typography.headlineMedium
                )

                if (state.mode == FlashMode.STROBE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Speed: ${state.strobeDelayMs}ms",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fast", style = MaterialTheme.typography.labelSmall)
                            Text("Slow", style = MaterialTheme.typography.labelSmall)
                        }
                        Slider(
                            value = state.strobeDelayMs.toFloat(),
                            onValueChange = { viewModel.setStrobeSpeed(it.toLong()) },
                            valueRange = 50f..500f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Warning: Strobe may cause discomfort",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
