package com.smarttoolkit.app.feature.bubblelevel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.util.rememberHaptic
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
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!state.isAvailable) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Accelerometer not available on this device.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val haptic = rememberHaptic()
                var wasLevel by remember { mutableStateOf(false) }
                var wasSideLevel by remember { mutableStateOf(false) }

                LaunchedEffect(state.isLevel) {
                    if (state.isLevel && !wasLevel) haptic()
                    wasLevel = state.isLevel
                }
                LaunchedEffect(state.isSideLevel) {
                    if (state.isSideLevel && !wasSideLevel) haptic()
                    wasSideLevel = state.isSideLevel
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calibrate button
                Button(onClick = viewModel::calibrate) {
                    Icon(
                        Icons.Filled.Adjust,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zero on flat surface")
                }
                if (state.isCalibrated) {
                    Text(
                        "Calibrated",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        "Place on a level surface, then tap to zero",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Surface bubble (phone flat on back) ---
                Text("Surface", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Phone flat on its back",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                SurfaceBubble(
                    pitch = state.pitch,
                    roll = state.roll,
                    isLevel = state.isLevel,
                    outlineColor = MaterialTheme.colorScheme.outline,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
                LevelStatusText(
                    isLevel = state.isLevel,
                    label = "Pitch: %.1f\u00B0  Roll: %.1f\u00B0".format(state.pitch, state.roll)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- Side bubble (phone on its side edge) ---
                Text("Side", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Phone on its left or right edge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                TubeBubble(
                    angle = state.sideAngle,
                    isLevel = state.isSideLevel,
                    outlineColor = MaterialTheme.colorScheme.outline,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
                LevelStatusText(
                    isLevel = state.isSideLevel,
                    label = "Tilt: %.1f\u00B0".format(state.sideAngle)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SurfaceBubble(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    outlineColor: Color,
    primaryColor: Color
) {
    val bubbleColor = if (isLevel) Color(0xFF4CAF50) else primaryColor

    Canvas(modifier = Modifier.size(220.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val vialRadius = min(size.width, size.height) / 2 - 8f
        val bubbleRadius = 24f

        // Outer vial circle
        drawCircle(
            color = outlineColor,
            radius = vialRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3f)
        )

        // Inner reference circle (target zone)
        drawCircle(
            color = outlineColor.copy(alpha = 0.3f),
            radius = vialRadius * 0.15f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Crosshair lines
        drawLine(outlineColor.copy(alpha = 0.3f), Offset(centerX - vialRadius, centerY), Offset(centerX + vialRadius, centerY), strokeWidth = 1f)
        drawLine(outlineColor.copy(alpha = 0.3f), Offset(centerX, centerY - vialRadius), Offset(centerX, centerY + vialRadius), strokeWidth = 1f)

        // Map pitch/roll to bubble position
        val maxOffset = vialRadius - bubbleRadius
        val rollOffset = (roll / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
        val pitchOffset = (pitch / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)

        val bubbleX = centerX + rollOffset
        val bubbleY = centerY - pitchOffset

        // Bubble
        drawCircle(color = bubbleColor, radius = bubbleRadius, center = Offset(bubbleX, bubbleY))
        drawCircle(color = bubbleColor.copy(alpha = 0.5f), radius = bubbleRadius, center = Offset(bubbleX, bubbleY), style = Stroke(width = 2f))
    }
}

@Composable
private fun TubeBubble(
    angle: Float,
    isLevel: Boolean,
    outlineColor: Color,
    primaryColor: Color
) {
    val bubbleColor = if (isLevel) Color(0xFF4CAF50) else primaryColor

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(48.dp)
    ) {
        val tubeHeight = size.height
        val tubeWidth = size.width
        val cornerRadius = tubeHeight / 2
        val bubbleWidth = tubeHeight * 1.8f
        val bubbleHeight = tubeHeight - 12f

        // Tube outline
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(0f, 0f),
            size = Size(tubeWidth, tubeHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 3f)
        )

        // Center reference line
        drawLine(
            outlineColor.copy(alpha = 0.4f),
            Offset(tubeWidth / 2, 4f),
            Offset(tubeWidth / 2, tubeHeight - 4f),
            strokeWidth = 2f
        )

        // Reference tick marks
        val tickSpacing = tubeWidth / 12
        for (i in 1..11) {
            if (i == 6) continue // skip center (drawn above)
            val x = i * tickSpacing
            val tickH = if (i % 3 == 0) tubeHeight * 0.3f else tubeHeight * 0.15f
            drawLine(
                outlineColor.copy(alpha = 0.2f),
                Offset(x, tubeHeight / 2 - tickH),
                Offset(x, tubeHeight / 2 + tickH),
                strokeWidth = 1f
            )
        }

        // Bubble position: map angle to horizontal offset
        val maxTravel = (tubeWidth - bubbleWidth) / 2
        val offset = (angle / 45f * maxTravel).coerceIn(-maxTravel, maxTravel)
        val bubbleX = (tubeWidth - bubbleWidth) / 2 + offset
        val bubbleY = (tubeHeight - bubbleHeight) / 2

        drawRoundRect(
            color = bubbleColor.copy(alpha = 0.7f),
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleHeight / 2, bubbleHeight / 2)
        )
        drawRoundRect(
            color = bubbleColor.copy(alpha = 0.4f),
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleHeight / 2, bubbleHeight / 2),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun LevelStatusText(isLevel: Boolean, label: String) {
    if (isLevel) {
        Text(
            "LEVEL",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF4CAF50)
        )
    } else {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
