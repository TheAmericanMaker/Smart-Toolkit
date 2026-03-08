package com.smartutilities.app.feature.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartutilities.app.ui.components.UtilityTopBar

@Composable
fun StorageScreen(
    onBack: () -> Unit,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Storage", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Internal Storage", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LinearProgressIndicator(
                        progress = { state.internalUsedPercent },
                        modifier = Modifier.fillMaxWidth().height(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${formatBytes(state.internalUsed)} used of ${formatBytes(state.internalTotal)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${formatBytes(state.internalAvailable)} available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.externalTotal > 0) {
                Text("External Storage", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val extPercent = if (state.externalTotal > 0)
                            state.externalUsed.toFloat() / state.externalTotal else 0f
                        LinearProgressIndicator(
                            progress = { extPercent },
                            modifier = Modifier.fillMaxWidth().height(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${formatBytes(state.externalUsed)} used of ${formatBytes(state.externalTotal)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${formatBytes(state.externalAvailable)} available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
