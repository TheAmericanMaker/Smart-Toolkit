package com.smarttoolkit.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Settings", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Use system theme", style = MaterialTheme.typography.bodyLarge)
                    Text("Follow device dark mode setting", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.useSystemTheme,
                    onCheckedChange = viewModel::setUseSystemTheme
                )
            }

            if (!state.useSystemTheme) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark mode", style = MaterialTheme.typography.bodyLarge)
                        Text("Use dark color scheme", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = state.darkMode,
                        onCheckedChange = viewModel::setDarkMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Smart Toolkit v1.0.0", style = MaterialTheme.typography.bodyLarge)
            Text(
                "A collection of handy everyday tools.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Provided by Mid Michigan MFG, LLC",
                style = MaterialTheme.typography.bodySmall
            )
            val uriHandler = LocalUriHandler.current
            val linkText = buildAnnotatedString {
                pushStringAnnotation(tag = "URL", annotation = "https://midmichiganmfg.com")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("midmichiganmfg.com")
                }
                pop()
            }
            ClickableText(
                text = linkText,
                style = MaterialTheme.typography.bodySmall,
                onClick = { offset ->
                    linkText.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()?.let { uriHandler.openUri(it.item) }
                }
            )
        }
    }
}
