package com.smarttoolkit.app.feature.notepad

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.data.model.NoteType
import com.smarttoolkit.app.ui.components.UtilityTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteListScreen(
    onBack: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit = {},
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportNotes(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importNotes(it) }
    }

    // Toast messages from export/import
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Show undo snackbar when a note is pending delete
    LaunchedEffect(uiState.pendingDeleteNote) {
        uiState.pendingDeleteNote?.let { note ->
            val result = snackbarHostState.showSnackbar(
                message = "Note deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Notepad",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            searchText = ""
                            viewModel.onSearchQueryChange("")
                        }
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Export notes") },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch("smart_toolkit_notes_backup.zip")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import notes") },
                            onClick = {
                                showMenu = false
                                importLauncher.launch(arrayOf("application/zip"))
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.filterType == NoteType.CHECKLIST) onNewChecklist() else onNewNote()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (uiState.filterType == NoteType.CHECKLIST) "New checklist" else "New note"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { text ->
                        searchText = text
                        viewModel.onSearchQueryChange(text)
                    },
                    label = { Text("Search notes") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.onFilterTypeChange(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.filterType == NoteType.TEXT,
                    onClick = {
                        viewModel.onFilterTypeChange(
                            if (uiState.filterType == NoteType.TEXT) null else NoteType.TEXT
                        )
                    },
                    label = { Text("Notes") },
                    leadingIcon = {
                        Icon(Icons.Filled.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = uiState.filterType == NoteType.CHECKLIST,
                    onClick = {
                        viewModel.onFilterTypeChange(
                            if (uiState.filterType == NoteType.CHECKLIST) null else NoteType.CHECKLIST
                        )
                    },
                    label = { Text("Checklists") },
                    leadingIcon = {
                        Icon(Icons.Filled.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }

            if (uiState.notes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No notes yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val pinnedNotes = uiState.notes.filter { it.isPinned }
                    val unpinnedNotes = uiState.notes.filter { !it.isPinned }

                    if (pinnedNotes.isNotEmpty()) {
                        item(key = "pinned_header") {
                            Text(
                                "Pinned",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                    .animateItem()
                            )
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            SwipeToDismissNoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onDelete = { viewModel.deleteNote(note) },
                                onTogglePin = { viewModel.togglePin(note) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                        item(key = "other_header") {
                            Text(
                                "Other",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                                    .animateItem()
                            )
                        }
                    }

                    items(unpinnedNotes, key = { it.id }) { note ->
                        SwipeToDismissNoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = { viewModel.deleteNote(note) },
                            onTogglePin = { viewModel.togglePin(note) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeToDismissNoteCard(
    note: com.smarttoolkit.app.data.db.NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        NoteCard(
            note = note,
            onClick = onClick,
            onDelete = onDelete,
            onTogglePin = onTogglePin
        )
    }
}

@Composable
private fun NoteCard(
    note: com.smarttoolkit.app.data.db.NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    val noteColor = note.colorLabel?.let {
        com.smarttoolkit.app.feature.notepad.smart.NoteCategorizer.getNoteColor(it)
    }
    val categoryColor = note.category?.let {
        com.smarttoolkit.app.feature.notepad.smart.NoteCategorizer.getCategoryColor(it)
    }
    val effectiveColor = noteColor ?: categoryColor

    val cardContainerColor = if (effectiveColor != null) {
        effectiveColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(
                    note.title.ifBlank { "Untitled" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (note.type == "CHECKLIST") {
                    Text(
                        "Checklist",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        note.content.take(80),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            overlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    Text(fmt.format(Date(note.updatedAt)), style = MaterialTheme.typography.labelSmall)
                    if (note.colorLabel != null) {
                        Text(
                            " · ${note.colorLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = noteColor ?: MaterialTheme.colorScheme.primary
                        )
                    } else if (note.category != null) {
                        Text(
                            " · ${note.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor ?: MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            leadingContent = {
                Icon(
                    imageVector = if (note.type == "CHECKLIST") Icons.Filled.Checklist else Icons.Filled.Notes,
                    contentDescription = null,
                    tint = effectiveColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Unpin" else "Pin",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        )
    }
}
