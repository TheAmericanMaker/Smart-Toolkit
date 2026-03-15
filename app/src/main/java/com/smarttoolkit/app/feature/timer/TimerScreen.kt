package com.smarttoolkit.app.feature.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.util.rememberHaptic

@Composable
fun TimerScreen(
    onBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = rememberHaptic()

    Scaffold(
        topBar = { UtilityTopBar(title = "Timer", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isConfiguring) {
                Text("Set Timer", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeField("H", state.hours) { viewModel.setHours(it) }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    TimeField("M", state.minutes) { viewModel.setMinutes(it) }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    TimeField("S", state.seconds) { viewModel.setSeconds(it) }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { haptic(); viewModel.start() }) { Text("Start") }
            } else if (state.isFinished) {
                Text("Time's Up!", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { haptic(); viewModel.dismissAlarm() }) { Text("Dismiss") }
            } else {
                Text(
                    text = state.displayTime,
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { haptic(); viewModel.cancel() }) { Text("Cancel") }
                    Button(onClick = { haptic(); if (state.isRunning) viewModel.pause() else viewModel.resume() }) {
                        Text(if (state.isRunning) "Pause" else "Resume")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }.take(2).toIntOrNull() ?: 0) },
        label = { Text(label) },
        modifier = Modifier.width(72.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}
