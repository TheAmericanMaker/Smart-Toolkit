package com.smarttoolkit.app.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.util.rememberHaptic

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = rememberHaptic()
    var showHistory by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Calculator",
                onBack = onBack,
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showHistory = !showHistory }) {
                            Icon(Icons.Filled.History, contentDescription = "History")
                        }
                    }
                    if (state.result.isNotEmpty()) {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("result", state.result))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy result")
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

            // History panel
            if (showHistory && history.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("History", style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { viewModel.clearHistory(); showHistory = false }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history",
                                modifier = Modifier.padding(0.dp))
                        }
                    }
                    HorizontalDivider()
                    LazyColumn {
                        items(history, key = { it.id }) { entry ->
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onHistoryItemClick(entry)
                                        showHistory = false
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(onClick = viewModel::toggleScientific) {
                Text(if (state.isScientific) "Basic" else "Scientific")
            }

            if (state.isScientific) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(").forEach { fn ->
                        OutlinedButton(
                            onClick = { haptic(); viewModel.onInput(fn) },
                            modifier = Modifier.weight(1f)
                        ) { Text(fn.removeSuffix("("), style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("^" to "^", "(" to "(", ")" to ")", "π" to "π", "e" to "e").forEach { (display, value) ->
                        OutlinedButton(
                            onClick = { haptic(); viewModel.onInput(value) },
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
                            "C" -> FilledTonalButton(onClick = { haptic(); viewModel.onClear() }, modifier = mod) { Text(display) }
                            "⌫" -> FilledTonalButton(onClick = { haptic(); viewModel.onBackspace() }, modifier = mod) { Text(display) }
                            "=" -> Button(onClick = { haptic(); viewModel.onEquals() }, modifier = mod) { Text(display) }
                            else -> OutlinedButton(onClick = { haptic(); viewModel.onInput(value) }, modifier = mod) { Text(display) }
                        }
                    }
                }
            }
        }
    }
}
