package com.smarttoolkit.app.feature.texttools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextToolsScreen(
    onBack: () -> Unit,
    viewModel: TextToolsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Text Tools", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::onTextChange,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                placeholder = { Text("Enter or paste text here...") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("${state.charCount} chars", style = MaterialTheme.typography.bodySmall)
                Text("${state.wordCount} words", style = MaterialTheme.typography.bodySmall)
                Text("${state.sentenceCount} sentences", style = MaterialTheme.typography.bodySmall)
                Text("${state.lineCount} lines", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Transform", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = viewModel::toUpperCase, label = { Text("UPPERCASE") })
                AssistChip(onClick = viewModel::toLowerCase, label = { Text("lowercase") })
                AssistChip(onClick = viewModel::toTitleCase, label = { Text("Title Case") })
                AssistChip(onClick = viewModel::reverse, label = { Text("Reverse") })
                AssistChip(onClick = viewModel::removeExtraSpaces, label = { Text("Trim Spaces") })
                AssistChip(onClick = viewModel::removeDuplicateLines, label = { Text("Dedup Lines") })
                AssistChip(onClick = viewModel::sortLines, label = { Text("Sort Lines") })
                AssistChip(onClick = viewModel::toggleFindReplace, label = { Text("Find & Replace") })
                AssistChip(onClick = viewModel::toggleWordFrequency, label = { Text("Word Freq") })
                AssistChip(onClick = viewModel::copyToClipboard, label = { Text(if (state.copied) "Copied!" else "Copy") })
                AssistChip(onClick = viewModel::clear, label = { Text("Clear") })
            }

            if (state.findReplaceVisible) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Find & Replace", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.findQuery,
                        onValueChange = viewModel::onFindQueryChanged,
                        label = { Text("Find") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.replaceQuery,
                        onValueChange = viewModel::onReplaceQueryChanged,
                        label = { Text("Replace") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.findQuery.isNotEmpty()) {
                        Text(
                            "${state.findMatchCount} matches",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = viewModel::replaceAll,
                        enabled = state.findQuery.isNotEmpty()
                    ) {
                        Text("Replace All")
                    }
                }
            }

            if (state.wordFrequencyVisible && state.wordFrequencies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Word Frequency (Top 30)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                state.wordFrequencies.forEach { (word, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(word, style = MaterialTheme.typography.bodyMedium)
                        Text(count.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
