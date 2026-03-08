package com.smartutilities.app.feature.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartutilities.app.ui.components.UtilityTopBar

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Calculator", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = state.expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.result.isNotEmpty()) {
                    Text(
                        text = "= ${state.result}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = viewModel::toggleScientific) {
                Text(if (state.isScientific) "Basic" else "Scientific")
            }

            if (state.isScientific) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(").forEach { fn ->
                        OutlinedButton(
                            onClick = { viewModel.onInput(fn) },
                            modifier = Modifier.weight(1f)
                        ) { Text(fn.removeSuffix("("), style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("^" to "^", "(" to "(", ")" to ")", "π" to "π", "e" to "e").forEach { (display, value) ->
                        OutlinedButton(
                            onClick = { viewModel.onInput(value) },
                            modifier = Modifier.weight(1f)
                        ) { Text(display, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }

            val buttons = listOf(
                listOf("C" to "C", "⌫" to "⌫", "%" to "%", "÷" to "÷"),
                listOf("7" to "7", "8" to "8", "9" to "9", "×" to "×"),
                listOf("4" to "4", "5" to "5", "6" to "6", "-" to "-"),
                listOf("1" to "1", "2" to "2", "3" to "3", "+" to "+"),
                listOf("0" to "0", "." to ".", "=" to "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (display, value) ->
                        val mod = if (value == "0") Modifier.weight(2f) else Modifier.weight(1f)
                        when (value) {
                            "C" -> FilledTonalButton(onClick = viewModel::onClear, modifier = mod) { Text(display) }
                            "⌫" -> FilledTonalButton(onClick = viewModel::onBackspace, modifier = mod) { Text(display) }
                            "=" -> Button(onClick = viewModel::onEquals, modifier = mod) { Text(display) }
                            else -> OutlinedButton(onClick = { viewModel.onInput(value) }, modifier = mod) { Text(display) }
                        }
                    }
                }
            }
        }
    }
}
