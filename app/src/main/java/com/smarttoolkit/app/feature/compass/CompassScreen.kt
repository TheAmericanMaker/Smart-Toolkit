package com.smarttoolkit.app.feature.compass

import android.hardware.SensorManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun CompassScreen(
    onBack: () -> Unit,
    viewModel: CompassViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Compass", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!state.isAvailable) {
                Text("Compass not available on this device", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = "${state.degrees}\u00B0 ${state.direction}",
                    style = MaterialTheme.typography.headlineLarge
                )

                // Accuracy indicator
                val accuracyText = when (state.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low accuracy - calibrate by moving phone in figure-8"
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable - calibrate by moving phone in figure-8"
                    else -> ""
                }
                val accuracyColor = when (state.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF4CAF50)
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFFFC107)
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Color(0xFFFF9800)
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                if (accuracyText.isNotEmpty()) {
                    Text(
                        accuracyText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accuracyColor
                    )
                }

                // True North toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (state.useTrueNorth) "True North" else "Magnetic North",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(
                        checked = state.useTrueNorth,
                        onCheckedChange = { viewModel.toggleTrueNorth() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val primary = MaterialTheme.colorScheme.primary
                val outline = MaterialTheme.colorScheme.outline
                val error = MaterialTheme.colorScheme.error
                val textMeasurer = rememberTextMeasurer()
                val lockedBearing = state.lockedBearing

                Canvas(modifier = Modifier.size(280.dp)) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2 - 20.dp.toPx()

                    // Outer circle
                    drawCircle(
                        color = outline,
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(2.dp.toPx())
                    )

                    // Locked bearing marker
                    if (lockedBearing != null) {
                        rotate(-state.azimuth + lockedBearing, Offset(centerX, centerY)) {
                            val markerY = centerY - radius + 4.dp.toPx()
                            drawCircle(
                                color = Color(0xFFFFC107),
                                radius = 6.dp.toPx(),
                                center = Offset(centerX, markerY)
                            )
                        }
                    }

                    // Rotate compass rose
                    rotate(-state.azimuth, Offset(centerX, centerY)) {
                        // Tick marks
                        for (deg in 0 until 360 step 10) {
                            val rad = Math.toRadians(deg.toDouble()).toFloat()
                            val inner = if (deg % 30 == 0) radius - 20.dp.toPx() else radius - 10.dp.toPx()
                            val cos = kotlin.math.cos(rad)
                            val sin = kotlin.math.sin(rad)
                            drawLine(
                                color = outline,
                                start = Offset(centerX + inner * sin, centerY - inner * cos),
                                end = Offset(centerX + radius * sin, centerY - radius * cos),
                                strokeWidth = if (deg % 30 == 0) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        // North needle (red triangle)
                        val needleLength = radius * 0.7f
                        val needleWidth = 12.dp.toPx()
                        val northPath = Path().apply {
                            moveTo(centerX, centerY - needleLength)
                            lineTo(centerX - needleWidth / 2, centerY)
                            lineTo(centerX + needleWidth / 2, centerY)
                            close()
                        }
                        drawPath(northPath, error)

                        // South needle
                        val southPath = Path().apply {
                            moveTo(centerX, centerY + needleLength)
                            lineTo(centerX - needleWidth / 2, centerY)
                            lineTo(centerX + needleWidth / 2, centerY)
                            close()
                        }
                        drawPath(southPath, outline.copy(alpha = 0.5f))

                        // Cardinal labels
                        drawCardinalLabel(textMeasurer, "N", 0f, centerX, centerY, radius - 35.dp.toPx(), error)
                        drawCardinalLabel(textMeasurer, "E", 90f, centerX, centerY, radius - 35.dp.toPx(), primary)
                        drawCardinalLabel(textMeasurer, "S", 180f, centerX, centerY, radius - 35.dp.toPx(), primary)
                        drawCardinalLabel(textMeasurer, "W", 270f, centerX, centerY, radius - 35.dp.toPx(), primary)
                    }

                    // Center dot
                    drawCircle(color = primary, radius = 4.dp.toPx(), center = Offset(centerX, centerY))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lock bearing button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AssistChip(
                        onClick = viewModel::toggleLockBearing,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (state.lockedBearing != null) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (state.lockedBearing != null) "Locked: %.0f\u00B0".format(state.lockedBearing)
                                    else "Lock Bearing"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCardinalLabel(
    textMeasurer: TextMeasurer,
    label: String,
    degrees: Float,
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color
) {
    val rad = Math.toRadians(degrees.toDouble()).toFloat()
    val x = cx + radius * kotlin.math.sin(rad)
    val y = cy - radius * kotlin.math.cos(rad)
    val style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = color)
    val result = textMeasurer.measure(label, style)
    drawText(result, topLeft = Offset(x - result.size.width / 2f, y - result.size.height / 2f))
}
