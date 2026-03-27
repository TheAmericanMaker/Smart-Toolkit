package com.smarttoolkit.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import com.smarttoolkit.app.ui.components.AdBanner
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUtilityClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showReorderDialog by remember { mutableStateOf(false) }

    if (showReorderDialog) {
        AlertDialog(
            onDismissRequest = { showReorderDialog = false },
            title = { Text("Reorder Favorites") },
            text = {
                Column {
                    state.favoriteUtilities.forEachIndexed { index, utility ->
                        if (index > 0) HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                utility.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(
                                onClick = { viewModel.reorderFavorite(index, index - 1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { viewModel.reorderFavorite(index, index + 1) },
                                enabled = index < state.favoriteUtilities.size - 1
                            ) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReorderDialog = false }) { Text("Done") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Toolkit") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search utilities...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (state.favoriteUtilities.isNotEmpty() && state.searchQuery.isBlank()) {
                    item(span = { GridItemSpan(3) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Favorites",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                            )
                            if (state.favoriteUtilities.size > 1) {
                                IconButton(onClick = { showReorderDialog = true }) {
                                    Icon(
                                        Icons.Filled.Reorder,
                                        contentDescription = "Reorder favorites"
                                    )
                                }
                            }
                        }
                    }
                    items(state.favoriteUtilities, key = { "fav_${it.id}" }) { utility ->
                        UtilityCard(
                            utility = utility,
                            isFavorite = true,
                            onClick = { onUtilityClick(utility.route) },
                            onFavoriteToggle = { viewModel.toggleFavorite(utility.id) }
                        )
                    }
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            "All Utilities",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                items(state.filteredUtilities, key = { it.id }) { utility ->
                    UtilityCard(
                        utility = utility,
                        isFavorite = utility.id in state.favorites,
                        onClick = { onUtilityClick(utility.route) },
                        onFavoriteToggle = { viewModel.toggleFavorite(utility.id) }
                    )
                }
            }

            if (!state.adsRemoved) {
                AdBanner(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
