package com.smartutilities.app.feature.bubblelevel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartutilities.app.ui.components.UtilityTopBar
import kotlin.math.min

@Composable
fun BubbleLevelScreen(
    onBack: () -> Unit,
    viewModel: BubbleLevelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Bubble Level", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!state.isAvailable) {
                Text("Accelerometer not available on this device.", style = MaterialTheme.typography.bodyLarge)
            } else {
                val bubbleColor = if (state.isLevel) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline

                Canvas(modifier = Modifier.size(280.dp)) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val vialRadius = min(size.width, size.height) / 2 - 8f
                    val bubbleRadius = 28f

                    // Outer vial circle
                    drawCircle(
                        color = outlineColor,
                        radius = vialRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3f)
                    )

                    // Inner reference circle
                    drawCircle(
                        color = outlineColor.copy(alpha = 0.3f),
                        radius = vialRadius * 0.15f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )

                    // Crosshair lines
                    drawLine(outlineColor.copy(alpha = 0.3f), Offset(centerX - vialRadius, centerY), Offset(centerX + vialRadius, centerY), strokeWidth = 1f)
                    drawLine(outlineColor.copy(alpha = 0.3f), Offset(centerX, centerY - vialRadius), Offset(centerX, centerY + vialRadius), strokeWidth = 1f)

                    // Map pitch/roll to bubble position (clamp to vial bounds)
                    val maxOffset = vialRadius - bubbleRadius
                    val rollOffset = (state.roll / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
                    val pitchOffset = (state.pitch / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)

                    val bubbleX = centerX + rollOffset
                    val bubbleY = centerY - pitchOffset

                    // Bubble
                    drawCircle(
                        color = bubbleColor,
                        radius = bubbleRadius,
                        center = Offset(bubbleX, bubbleY)
                    )
                    drawCircle(
                        color = bubbleColor.copy(alpha = 0.5f),
                        radius = bubbleRadius,
                        center = Offset(bubbleX, bubbleY),
                        style = Stroke(width = 2f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isLevel) {
                    Text(
                        "LEVEL",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        "Pitch: %.1f\u00B0  Roll: %.1f\u00B0".format(state.pitch, state.roll),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
