package com.smartutilities.app.feature.ruler

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartutilities.app.ui.components.UtilityTopBar

@Composable
fun RulerScreen(
    onBack: () -> Unit,
    viewModel: RulerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Ruler", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                FilterChip(
                    selected = state.isMetric,
                    onClick = { if (!state.isMetric) viewModel.toggleUnit() },
                    label = { Text("cm / mm") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { if (state.isMetric) viewModel.toggleUnit() },
                    label = { Text("inches") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val density = LocalDensity.current
            val primary = MaterialTheme.colorScheme.primary
            val onSurface = MaterialTheme.colorScheme.onSurface
            val textMeasurer = rememberTextMeasurer()

            Canvas(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                val ydpi = density.density * 160f
                val pxPerCm = ydpi / 2.54f
                val pxPerInch = ydpi

                if (state.isMetric) {
                    val totalCm = (size.height / pxPerCm).toInt()
                    for (mm in 0..totalCm * 10) {
                        val y = mm * pxPerCm / 10f
                        val tickLen = when {
                            mm % 10 == 0 -> 40.dp.toPx()
                            mm % 5 == 0 -> 25.dp.toPx()
                            else -> 15.dp.toPx()
                        }
                        drawLine(primary, Offset(0f, y), Offset(tickLen, y), strokeWidth = 1.5f)
                        if (mm % 10 == 0) {
                            val cm = mm / 10
                            val label = "$cm cm"
                            val result = textMeasurer.measure(
                                label,
                                TextStyle(
                                    fontSize = 12.sp,
                                    color = onSurface,
                                    fontWeight = if (cm == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            drawText(result, topLeft = Offset(tickLen + 4.dp.toPx(), y - result.size.height / 2f))
                        }
                    }
                } else {
                    val totalInches = (size.height / pxPerInch).toInt()
                    for (eighth in 0..totalInches * 8) {
                        val y = eighth * pxPerInch / 8f
                        val tickLen = when {
                            eighth % 8 == 0 -> 40.dp.toPx()
                            eighth % 4 == 0 -> 30.dp.toPx()
                            eighth % 2 == 0 -> 20.dp.toPx()
                            else -> 12.dp.toPx()
                        }
                        drawLine(primary, Offset(0f, y), Offset(tickLen, y), strokeWidth = 1.5f)
                        if (eighth % 8 == 0) {
                            val inches = eighth / 8
                            val label = if (inches == 0) "0 in" else "$inches in"
                            val result = textMeasurer.measure(
                                label,
                                TextStyle(
                                    fontSize = 12.sp,
                                    color = onSurface,
                                    fontWeight = if (inches == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            drawText(result, topLeft = Offset(tickLen + 4.dp.toPx(), y - result.size.height / 2f))
                        }
                    }
                }
            }
        }
    }
}
