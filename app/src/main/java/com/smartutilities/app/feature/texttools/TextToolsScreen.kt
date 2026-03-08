package com.smartutilities.app.feature.texttools

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
import androidx.compose.material3.AssistChip
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
import com.smartutilities.app.ui.components.UtilityTopBar

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
                AssistChip(onClick = viewModel::copyToClipboard, label = { Text(if (state.copied) "Copied!" else "Copy") })
                AssistChip(onClick = viewModel::clear, label = { Text("Clear") })
            }
        }
    }
}
