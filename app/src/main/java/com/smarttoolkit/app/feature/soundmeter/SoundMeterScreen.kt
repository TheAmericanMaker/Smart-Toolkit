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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val primary = MaterialTheme.colorScheme.primary
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = surfaceVariant,
                startAngle = 135f, sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val sweep = (state.currentDb / 120.0 * 270).toFloat()
            drawArc(
                color = primary,
                startAngle = 135f, sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "%.0f dB".format(state.currentDb),
            style = MaterialTheme.typography.displayMedium,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Approximate readings only",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state.isRecording) {
            OutlinedButton(onClick = viewModel::stopRecording) { Text("Stop") }
        } else {
            Button(onClick = viewModel::startRecording) { Text("Start") }
        }
    }
}
