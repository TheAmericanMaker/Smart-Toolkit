package com.smarttoolkit.app.feature.randomgenerator

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.util.rememberHaptic

@Composable
fun RandomGeneratorScreen(
    onBack: () -> Unit,
    viewModel: RandomGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = rememberHaptic()

    Scaffold(
        topBar = { UtilityTopBar(title = "Random Generator", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(selectedTabIndex = state.mode.ordinal) {
                RandomMode.entries.forEach { mode ->
                    Tab(
                        selected = state.mode == mode,
                        onClick = { viewModel.setMode(mode) },
                        text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.mode) {
                    RandomMode.NUMBER -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.min, onValueChange = viewModel::setMin,
                                label = { Text("Min") }, modifier = Modifier.width(120.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                            )
                            OutlinedTextField(
                                value = state.max, onValueChange = viewModel::setMax,
                                label = { Text("Max") }, modifier = Modifier.width(120.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                            )
                        }
                    }
                    RandomMode.DICE -> Text("Roll a 6-sided die", style = MaterialTheme.typography.bodyLarge)
                    RandomMode.COIN -> Text("Flip a coin", style = MaterialTheme.typography.bodyLarge)
                    RandomMode.PASSWORD -> {
                        OutlinedTextField(
                            value = state.passwordLength, onValueChange = viewModel::setPasswordLength,
                            label = { Text("Length") }, modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.includeUppercase, onCheckedChange = { viewModel.toggleUppercase() })
                            Text("A-Z"); Spacer(Modifier.width(8.dp))
                            Checkbox(checked = state.includeLowercase, onCheckedChange = { viewModel.toggleLowercase() })
                            Text("a-z"); Spacer(Modifier.width(8.dp))
                            Checkbox(checked = state.includeDigits, onCheckedChange = { viewModel.toggleDigits() })
                            Text("0-9"); Spacer(Modifier.width(8.dp))
                            Checkbox(checked = state.includeSymbols, onCheckedChange = { viewModel.toggleSymbols() })
                            Text("!@#")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { haptic(); viewModel.generate() }) { Text("Generate") }
                Spacer(modifier = Modifier.height(24.dp))

                if (state.result.isNotEmpty()) {
                    val context = LocalContext.current
                    Text(
                        text = state.result,
                        style = if (state.mode == RandomMode.PASSWORD) MaterialTheme.typography.bodyLarge
                        else MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("generated", state.result))
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }
    }
}
