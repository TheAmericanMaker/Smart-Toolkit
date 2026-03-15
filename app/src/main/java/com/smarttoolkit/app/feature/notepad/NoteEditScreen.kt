package com.smarttoolkit.app.feature.notepad

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.data.model.NoteType
import com.smarttoolkit.app.feature.notepad.components.ChecklistItemRow
import com.smarttoolkit.app.feature.notepad.components.FullScreenImageViewer
import com.smarttoolkit.app.feature.notepad.components.ImageAttachmentRow
import com.smarttoolkit.app.feature.notepad.smart.ChecklistSuggestionProvider
import com.smarttoolkit.app.feature.notepad.smart.NoteCategorizer
import com.smarttoolkit.app.feature.notepad.templates.TemplatePickerBottomSheet
import com.smarttoolkit.app.ui.components.UtilityTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditScreen(
    onBack: () -> Unit,
    viewModel: NotepadViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ocrHintShown by viewModel.showOcrHint.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showTemplates by rememberSaveable { mutableStateOf(false) }
    var viewingImageIndex by remember { mutableIntStateOf(-1) }
    var contentFieldValue by remember { mutableStateOf(TextFieldValue(state.content)) }

    // Sync from ViewModel when content changes externally (template, OCR, etc.)
    LaunchedEffect(state.content) {
        if (contentFieldValue.text != state.content) {
            contentFieldValue = TextFieldValue(state.content, TextRange(state.content.length))
        }
    }

    // Show OCR hint when images are first added
    LaunchedEffect(state.images.size, ocrHintShown) {
        if (state.images.isNotEmpty() && !ocrHintShown) {
            snackbarHostState.showSnackbar(
                message = "Tip: Tap an image to extract text from it",
            )
            viewModel.dismissOcrHint()
        }
    }

    val focusRequesters = remember(state.checklistItems.size) {
        List(state.checklistItems.size) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        viewModel.focusItemIndex.collect { index ->
            if (index in focusRequesters.indices) {
                try {
                    focusRequesters[index].requestFocus()
                } catch (_: Exception) {}
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImageFromUri(it) }
    }

    var dictationTarget by remember { mutableStateOf("content") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            text?.let {
                if (dictationTarget == "title") {
                    viewModel.onTitleChange(
                        if (state.title.isBlank()) it else state.title + " " + it
                    )
                } else if (state.type == NoteType.TEXT) {
                    // Insert at cursor position
                    val selection = contentFieldValue.selection
                    val before = contentFieldValue.text.substring(0, selection.start)
                    val after = contentFieldValue.text.substring(selection.end)
                    val newText = before + it + after
                    val newCursor = selection.start + it.length
                    contentFieldValue = TextFieldValue(newText, TextRange(newCursor))
                    viewModel.onContentChange(newText)
                } else {
                    viewModel.onDictatedText(it)
                }
            }
        }
    }

    val launchDictation: (String) -> Unit = { target ->
        dictationTarget = target
        val prompt = if (target == "title") "Speak your title..." else "Speak your note..."
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        speechLauncher.launch(intent)
    }

    BackHandler {
        viewModel.save()
        onBack()
    }

    // Template picker
    if (showTemplates) {
        TemplatePickerBottomSheet(
            onDismiss = { showTemplates = false },
            onTemplateSelected = { template ->
                viewModel.applyTemplate(template.title, template.type, template.items)
                showTemplates = false
            }
        )
    }

    // Full-screen image viewer
    if (viewingImageIndex >= 0 && viewingImageIndex < state.images.size) {
        val img = state.images[viewingImageIndex]
        val file = viewModel.getImageFile(img.filePath)
        if (file.exists()) {
            FullScreenImageViewer(
                imageFile = file,
                onDismiss = { viewingImageIndex = -1 },
                onTextExtracted = { text -> viewModel.onExtractedText(text) }
            )
        }
    }

    val title = when {
        state.isNew && state.type == NoteType.CHECKLIST -> "New Checklist"
        state.isNew -> "New Note"
        state.type == NoteType.CHECKLIST -> "Edit Checklist"
        else -> "Edit Note"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UtilityTopBar(
                title = title,
                onBack = {
                    viewModel.save()
                    onBack()
                },
                actions = {
                    // Type toggle
                    IconButton(onClick = { viewModel.onToggleType() }) {
                        Icon(
                            imageVector = if (state.type == NoteType.TEXT) Icons.Filled.Checklist else Icons.Filled.Notes,
                            contentDescription = if (state.type == NoteType.TEXT) "Switch to checklist" else "Switch to note"
                        )
                    }
                    // Share
                    IconButton(
                        onClick = {
                            val text = NoteShareFormatter.formatForSharing(
                                state.title, state.content, state.type, state.checklistItems
                            )
                            val html = NoteShareFormatter.formatAsHtml(
                                state.title, state.content, state.type, state.checklistItems
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, state.title)
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_HTML_TEXT, html)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        },
                        enabled = state.title.isNotBlank() || state.content.isNotBlank() ||
                            state.checklistItems.any { it.text.isNotBlank() }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Template button for new notes
            AnimatedVisibility(
                visible = state.isNew && state.title.isBlank() && state.content.isBlank() &&
                    state.checklistItems.all { it.text.isBlank() }
            ) {
                TextButton(
                    onClick = { showTemplates = true },
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text("Use a template")
                }
            }

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                trailingIcon = {
                    IconButton(onClick = { launchDictation("title") }) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Voice input for title",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Color picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "No color" option
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (state.colorLabel == null) 2.dp else 1.dp,
                            color = if (state.colorLabel == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable { viewModel.onColorLabelChange(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.FormatColorReset,
                        contentDescription = "No color",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                NoteCategorizer.noteColors.forEach { (label, color) ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (state.colorLabel == label) 2.dp else 0.dp,
                                color = if (state.colorLabel == label) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.onColorLabelChange(label) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.colorLabel == label) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = label,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Image attachments
            if (state.images.isNotEmpty() || state.type != NoteType.TEXT) {
                ImageAttachmentRow(
                    images = state.images,
                    getImageFile = { viewModel.getImageFile(it) },
                    onAddImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveImage = { viewModel.removeImage(it) },
                    onImageClick = { viewingImageIndex = it }
                )
            }

            when (state.type) {
                NoteType.TEXT -> {
                    // Add image button for text notes
                    if (state.images.isEmpty()) {
                        TextButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text("Attach image")
                        }
                    }
                    OutlinedTextField(
                        value = contentFieldValue,
                        onValueChange = { newValue ->
                            contentFieldValue = newValue
                            viewModel.onContentChange(newValue.text)
                        },
                        label = { Text("Content") },
                        trailingIcon = {
                            IconButton(onClick = { launchDictation("content") }) {
                                Icon(
                                    Icons.Filled.Mic,
                                    contentDescription = "Voice input",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                NoteType.CHECKLIST -> {
                    // Suggestions
                    val suggestions = remember(state.title) {
                        ChecklistSuggestionProvider.getSuggestions(state.title)
                    }
                    val addedTexts = remember(state.checklistItems) {
                        state.checklistItems.map { it.text.lowercase() }.toSet()
                    }
                    val filteredSuggestions = suggestions.filter { it.lowercase() !in addedTexts }

                    AnimatedVisibility(visible = filteredSuggestions.isNotEmpty()) {
                        Column {
                            Text(
                                "Suggestions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            FlowRow(
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                filteredSuggestions.take(8).forEach { suggestion ->
                                    AssistChip(
                                        onClick = { viewModel.addSuggestedItem(suggestion) },
                                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Checklist items
                    val uncheckedItems = state.checklistItems.withIndex().filter { !it.value.isChecked }
                    val checkedItems = state.checklistItems.withIndex().filter { it.value.isChecked }

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Unchecked items first
                        itemsIndexed(
                            uncheckedItems,
                            key = { _, indexed -> indexed.value.tempId }
                        ) { _, indexed ->
                            val actualIndex = indexed.index
                            ChecklistItemRow(
                                text = indexed.value.text,
                                isChecked = false,
                                onTextChange = { viewModel.onChecklistItemTextChange(actualIndex, it) },
                                onCheckedChange = { viewModel.onChecklistItemCheckedChange(actualIndex, it) },
                                onEnterPressed = { viewModel.onAddChecklistItem(actualIndex) },
                                onDelete = { viewModel.onDeleteChecklistItem(actualIndex) },
                                canDelete = state.checklistItems.size > 1,
                                focusRequester = if (actualIndex < focusRequesters.size) focusRequesters[actualIndex] else FocusRequester()
                            )
                        }

                        // Add item button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.onAddChecklistItem() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "Add item",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { launchDictation("content") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Voice input",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Checked items section
                        if (checkedItems.isNotEmpty()) {
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    "${checkedItems.size} checked item${if (checkedItems.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                )
                            }
                            itemsIndexed(
                                checkedItems,
                                key = { _, indexed -> indexed.value.tempId }
                            ) { _, indexed ->
                                val actualIndex = indexed.index
                                ChecklistItemRow(
                                    text = indexed.value.text,
                                    isChecked = true,
                                    onTextChange = { viewModel.onChecklistItemTextChange(actualIndex, it) },
                                    onCheckedChange = { viewModel.onChecklistItemCheckedChange(actualIndex, it) },
                                    onEnterPressed = {},
                                    onDelete = { viewModel.onDeleteChecklistItem(actualIndex) },
                                    canDelete = state.checklistItems.size > 1,
                                    focusRequester = if (actualIndex < focusRequesters.size) focusRequesters[actualIndex] else FocusRequester()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
