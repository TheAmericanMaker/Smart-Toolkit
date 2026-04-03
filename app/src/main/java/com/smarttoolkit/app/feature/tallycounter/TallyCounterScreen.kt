package com.smarttoolkit.app.feature.tallycounter

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.RequestNotificationPermission
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun TallyCounterScreen(
    onBack: () -> Unit,
    viewModel: TallyCounterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showResetDialog by remember { mutableStateOf(false) }
    var resetCounterId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    RequestNotificationPermission()

    if (showResetDialog && resetCounterId != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false; resetCounterId = null },
            title = { Text("Reset Counter") },
            text = { Text("Reset this counter to 0?") },
            confirmButton = {
                TextButton(onClick = {
                    resetCounterId?.let { viewModel.resetCounter(it) }
                    showResetDialog = false
                    resetCounterId = null
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; resetCounterId = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddDialog) {
        var counterName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Counter") },
            text = {
                OutlinedTextField(
                    value = counterName,
                    onValueChange = { counterName = it },
                    label = { Text("Counter name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = counterName.trim().ifEmpty { "Counter ${state.counters.size + 1}" }
                        viewModel.addCounter(name)
                        showAddDialog = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Tally Counter",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (viewModel.isNotificationActive) {
                                viewModel.stopNotificationService()
                            } else {
                                viewModel.startNotificationService()
                            }
                        }
                    ) {
                        Icon(
                            if (viewModel.isNotificationActive) Icons.Filled.Notifications
                            else Icons.Filled.NotificationsOff,
                            contentDescription = if (viewModel.isNotificationActive) "Unpin from notifications"
                            else "Pin to notifications"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add counter")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = state.counters,
                key = { it.id }
            ) { counter ->
                val canDelete = state.counters.size > 1
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart && canDelete) {
                            viewModel.deleteCounter(counter.id)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> Color.Transparent
                            },
                            label = "swipe-bg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, shape = MaterialTheme.shapes.medium)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    },
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = canDelete
                ) {
                    CounterCard(
                        counter = counter,
                        onIncrement = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.incrementCounter(counter.id)
                        },
                        onDecrement = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.decrementCounter(counter.id)
                        },
                        onReset = {
                            resetCounterId = counter.id
                            showResetDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterCard(
    counter: TallyCounterItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = counter.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = onReset,
                    enabled = counter.count > 0
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = counter.count.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 64.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrement", modifier = Modifier.size(28.dp))
                }
                Button(
                    onClick = onIncrement,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increment", modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
