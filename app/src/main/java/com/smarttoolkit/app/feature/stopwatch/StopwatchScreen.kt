package com.smarttoolkit.app.feature.stopwatch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun StopwatchScreen(
    onBack: () -> Unit,
    viewModel: StopwatchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Stopwatch",
                onBack = onBack,
                actions = {
                    if (state.laps.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = state.laps.joinToString("\n") { lap ->
                                "Lap ${lap.lapNumber}: ${formatTime(lap.splitMs)} (Total: ${formatTime(lap.totalMs)})"
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("laps", text))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy laps")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = state.displayTime,
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = viewModel::reset) { Text("Reset") }
                Button(onClick = viewModel::startStop) {
                    Text(if (state.isRunning) "Stop" else "Start")
                }
                OutlinedButton(
                    onClick = viewModel::lap,
                    enabled = state.isRunning
                ) { Text("Lap") }
            }

            if (state.laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.laps.reversed()) { lap ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lap ${lap.lapNumber}", fontFamily = FontFamily.Monospace)
                            Text(formatTime(lap.splitMs), fontFamily = FontFamily.Monospace)
                            Text(formatTime(lap.totalMs), fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
