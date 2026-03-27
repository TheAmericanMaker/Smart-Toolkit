package com.smarttoolkit.app.ui.settings

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.data.billing.BillingState
import com.smarttoolkit.app.ui.components.UtilityTopBar
import com.smarttoolkit.app.ui.theme.AppColorTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onUserGuide: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

            Spacer(modifier = Modifier.height(16.dp))
            Text("Color theme", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Choose an accent color for the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Filter out Dynamic on pre-Android 12 (no Material You support)
            val themes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AppColorTheme.entries
            } else {
                AppColorTheme.entries.filter { it != AppColorTheme.DYNAMIC }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { theme ->
                    val isSelected = state.colorTheme == theme.name
                    ColorThemeCircle(
                        color = theme.previewColor,
                        label = theme.label,
                        isSelected = isSelected,
                        onClick = { viewModel.setColorTheme(theme.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Help", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onUserGuide,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("User Guide")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Purchases", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (state.adsRemoved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Ads Removed", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Thank you for your support!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.purchaseRemoveAds(context as Activity) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.billingState !is BillingState.Pending
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        if (state.billingState is BillingState.Pending) "Purchase Pending..."
                        else "Remove Ads  \u2013  \$1.99"
                    )
                }
                if (state.billingState is BillingState.Error) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Purchase failed. Please try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
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
                "Provided by The American Maker & Claude Code",
                style = MaterialTheme.typography.bodySmall
            )
            val uriHandler = LocalUriHandler.current
            val linkText = buildAnnotatedString {
                pushStringAnnotation(tag = "URL", annotation = "https://www.youtube.com/@AmericanMaking")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("https://www.youtube.com/@AmericanMaking")
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

@Composable
private fun ColorThemeCircle(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
