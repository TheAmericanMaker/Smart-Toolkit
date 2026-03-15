package com.smarttoolkit.app.feature.soundmeter

import android.Manifest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.PermissionHandler
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun SoundMeterScreen(
    onBack: () -> Unit,
    viewModel: SoundMeterViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { UtilityTopBar(title = "Sound Meter", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PermissionHandler(
                permission = Manifest.permission.RECORD_AUDIO,
                rationaleText = "Microphone permission is needed to measure sound levels."
            ) {
                SoundMeterContent(viewModel)
            }
        }
    }
}

@Composable
private fun SoundMeterContent(viewModel: SoundMeterViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val primary = MaterialTheme.colorScheme.primary
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

        // Arc gauge
        Canvas(modifier = Modifier.size(180.dp)) {
            val stroke = 14.dp.toPx()
            drawArc(
                color = surfaceVariant,
                startAngle = 135f, sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val arcColor = when {
                state.currentDb > 85 -> Color(0xFFF44336)
                state.currentDb > 60 -> Color(0xFFFFC107)
                else -> Color(0xFF4CAF50)
            }
            val sweep = (state.currentDb / 120.0 * 270).toFloat()
            drawArc(
                color = arcColor,
                startAngle = 135f, sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }

        Text(
            "%.0f dB".format(state.currentDb),
            style = MaterialTheme.typography.displayMedium,
            fontFamily = FontFamily.Monospace
        )

        // Level description
        val levelLabel = when {
            state.currentDb > 100 -> "Extremely loud"
            state.currentDb > 85 -> "Very loud - hearing damage risk"
            state.currentDb > 70 -> "Loud"
            state.currentDb > 50 -> "Moderate"
            state.currentDb > 30 -> "Quiet"
            else -> "Very quiet"
        }
        Text(
            levelLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Min", style = MaterialTheme.typography.labelMedium)
                Text(
                    if (state.minDb == Double.MAX_VALUE) "--" else "%.0f dB".format(state.minDb),
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Max", style = MaterialTheme.typography.labelMedium)
                Text("%.0f dB".format(state.maxDb), fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live chart
        if (state.dbHistory.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("History", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    val chartPrimary = primary
                    val chartSurfaceVariant = surfaceVariant
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val history = state.dbHistory
                        if (history.size < 2) return@Canvas

                        // Grid lines at 30, 60, 90 dB
                        listOf(30.0, 60.0, 90.0).forEach { level ->
                            val y = size.height * (1f - (level / 120.0).toFloat())
                            drawLine(
                                chartSurfaceVariant,
                                Offset(0f, y),
                                Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Draw the waveform path
                        val path = Path()
                        val stepX = size.width / (history.size - 1).coerceAtLeast(1)
                        history.forEachIndexed { index, db ->
                            val x = index * stepX
                            val y = size.height * (1f - (db / 120.0).toFloat())
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, chartPrimary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Approximate readings only",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isRecording) {
            OutlinedButton(onClick = viewModel::stopRecording) { Text("Stop") }
        } else {
            Button(onClick = viewModel::startRecording) { Text("Start") }
        }
    }
}
