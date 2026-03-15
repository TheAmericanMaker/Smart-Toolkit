package com.smarttoolkit.app.feature.timer

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.util.rememberHaptic

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = rememberHaptic()
    var showSoundPicker by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(16.dp))

                // Quick presets
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 5, 10, 15, 30).forEach { mins ->
                        AssistChip(
                            onClick = { viewModel.applyPreset(mins) },
                            label = { Text("${mins}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                // Alarm sound selector
                OutlinedButton(onClick = { showSoundPicker = true }) {
                    val soundName = if (state.availableSounds.isNotEmpty() &&
                        state.selectedSoundIndex in state.availableSounds.indices
                    ) {
                        state.availableSounds[state.selectedSoundIndex].title
                    } else "Default Alarm"
                    Text("Sound: $soundName", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { haptic(); viewModel.start() }) { Text("Start") }
            } else if (state.isFinished) {
                Text("Time's Up!", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { haptic(); viewModel.stopAlarm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Stop Alarm") }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { haptic(); viewModel.dismissAlarm() }) { Text("Dismiss") }
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

    // Sound picker bottom sheet
    if (showSoundPicker) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.stopAlarm()
                showSoundPicker = false
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Select Alarm Sound",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    itemsIndexed(state.availableSounds) { index, sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSound(index)
                                    viewModel.previewSound(index)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == state.selectedSoundIndex,
                                onClick = {
                                    viewModel.selectSound(index)
                                    viewModel.previewSound(index)
                                }
                            )
                            Text(
                                text = sound.title,
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(onClick = { viewModel.previewSound(index) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Preview")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
