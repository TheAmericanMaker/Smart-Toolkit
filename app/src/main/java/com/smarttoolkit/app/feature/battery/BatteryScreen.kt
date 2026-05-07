package com.smarttoolkit.app.feature.battery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun BatteryScreen(
    onBack: () -> Unit,
    viewModel: BatteryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var showHistory by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Battery",
                onBack = onBack,
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showHistory = !showHistory }) {
                            Icon(Icons.Filled.History, contentDescription = "History")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (showHistory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Battery History", style = MaterialTheme.typography.titleMedium)
                    Row {
                        IconButton(onClick = { viewModel.clearHistory(); showHistory = false }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear all")
                        }
                        OutlinedButton(onClick = { showHistory = false }) { Text("Back") }
                    }
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    val fmt = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
                    history.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(
                                fmt.format(java.util.Date(entry.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < history.lastIndex) HorizontalDivider()
                    }
                }
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val arcColor = when {
                state.percentage > 50 -> Color(0xFF4CAF50)
                state.percentage > 20 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.size(160.dp)) {
                val stroke = 12.dp.toPx()
                drawArc(
                    color = surfaceVariant,
                    startAngle = 135f, sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = arcColor,
                    startAngle = 135f, sweepAngle = 270f * state.percentage / 100f,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Text("${state.percentage}%", style = MaterialTheme.typography.displayMedium)
            Text(state.status, style = MaterialTheme.typography.titleMedium)
            if (state.estimatedTimeRemaining.isNotEmpty()) {
                Text(
                    state.estimatedTimeRemaining,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val items = listOf(
                "Plugged" to state.plugged,
                "Temperature" to state.temperatureDisplay,
                "Voltage" to "${state.voltage} mV",
                "Health" to state.health,
                "Technology" to state.technology
            )
            items.forEach { (label, value) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (label == "Temperature") Modifier.clickable { viewModel.toggleTemperatureUnit() }
                                else Modifier
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Row {
                            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            if (label == "Temperature") {
                                Text(
                                    "  tap to switch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        } // end else (not showHistory)
    }
}
