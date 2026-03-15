package com.smarttoolkit.app.feature.notepad

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.model.ChecklistItem
import com.smarttoolkit.app.data.model.Note
import com.smarttoolkit.app.data.model.NoteImage
import com.smarttoolkit.app.data.model.NoteType
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import com.smarttoolkit.app.data.repository.NoteRepository
import com.smarttoolkit.app.feature.notepad.smart.NoteCategorizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class ChecklistItemUiState(
    val id: Long = 0,
    val tempId: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

data class NoteImageUiState(
    val id: Long = 0,
    val filePath: String = "",
    val isNew: Boolean = false
)

data class NoteEditUiState(
    val title: String = "",
    val content: String = "",
    val type: NoteType = NoteType.TEXT,
    val category: String? = null,
    val isPinned: Boolean = false,
    val checklistItems: List<ChecklistItemUiState> = emptyList(),
    val images: List<NoteImageUiState> = emptyList(),
    val isNew: Boolean = true,
    val isLoaded: Boolean = false
)

@HiltViewModel
class NotepadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<String>("noteId")?.toLongOrNull() ?: -1L
    private val initialType: String = savedStateHandle.get<String>("type") ?: "TEXT"

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    private val _focusItemIndex = MutableSharedFlow<Int>()
    val focusItemIndex = _focusItemIndex.asSharedFlow()

    val showOcrHint: StateFlow<Boolean> = preferencesRepository.ocrHintShown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun dismissOcrHint() {
        viewModelScope.launch {
            preferencesRepository.setOcrHintShown()
        }
    }

    private var savedNoteId: Long = noteId
    private var autoSaveJob: Job? = null

    init {
        if (noteId > 0) {
            viewModelScope.launch {
                repository.getNoteWithItems(noteId)?.let { note ->
                    _uiState.value = NoteEditUiState(
                        title = note.title,
                        content = note.content,
                        type = note.type,
                        category = note.category,
                        isPinned = note.isPinned,
                        checklistItems = note.checklistItems.map {
                            ChecklistItemUiState(id = it.id, text = it.text, isChecked = it.isChecked)
                        }.ifEmpty { listOf(ChecklistItemUiState()) },
                        images = note.images.map {
                            NoteImageUiState(id = it.id, filePath = it.filePath)
                        },
                        isNew = false,
                        isLoaded = true
                    )
                }
            }
        } else {
            val type = try { NoteType.valueOf(initialType) } catch (_: Exception) { NoteType.TEXT }
            _uiState.value = NoteEditUiState(
                type = type,
                checklistItems = if (type == NoteType.CHECKLIST) listOf(ChecklistItemUiState()) else emptyList(),
                isLoaded = true
            )
        }
    }

    fun onTitleChange(title: String) {
        val category = NoteCategorizer.categorize(title)
        _uiState.value = _uiState.value.copy(title = title, category = category)
        scheduleAutoSave()
    }

    fun onContentChange(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
        scheduleAutoSave()
    }

    fun onToggleType() {
        val state = _uiState.value
        if (state.type == NoteType.TEXT) {
            val lines = state.content.lines().filter { it.isNotBlank() }
            val items = lines.map { ChecklistItemUiState(text = it) }.ifEmpty { listOf(ChecklistItemUiState()) }
            _uiState.value = state.copy(type = NoteType.CHECKLIST, checklistItems = items, content = "")
        } else {
            val content = state.checklistItems
                .filter { it.text.isNotBlank() }
                .joinToString("\n") { it.text }
            _uiState.value = state.copy(type = NoteType.TEXT, content = content, checklistItems = emptyList())
        }
        scheduleAutoSave()
    }

    fun onChecklistItemTextChange(index: Int, text: String) {
        val items = _uiState.value.checklistItems.toMutableList()
        if (index < items.size) {
            items[index] = items[index].copy(text = text)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun onChecklistItemCheckedChange(index: Int, checked: Boolean) {
        val items = _uiState.value.checklistItems.toMutableList()
        if (index < items.size) {
            items[index] = items[index].copy(isChecked = checked)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun onAddChecklistItem(afterIndex: Int = -1) {
        val items = _uiState.value.checklistItems.toMutableList()
        val insertAt = if (afterIndex >= 0) afterIndex + 1 else items.size
        val newItem = ChecklistItemUiState()
        items.add(insertAt, newItem)
        _uiState.value = _uiState.value.copy(checklistItems = items)
        viewModelScope.launch {
            _focusItemIndex.emit(insertAt)
        }
    }

    fun onDeleteChecklistItem(index: Int) {
        val items = _uiState.value.checklistItems.toMutableList()
        if (items.size > 1 && index < items.size) {
            items.removeAt(index)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun onReorderChecklistItems(fromIndex: Int, toIndex: Int) {
        val items = _uiState.value.checklistItems.toMutableList()
        if (fromIndex < items.size && toIndex < items.size) {
            val item = items.removeAt(fromIndex)
            items.add(toIndex, item)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun addImageFromUri(uri: Uri) {
        viewModelScope.launch {
            val filePath = saveImageToInternal(uri) ?: return@launch
            val images = _uiState.value.images.toMutableList()
            if (images.size >= 5) return@launch
            images.add(NoteImageUiState(filePath = filePath, isNew = true))
            _uiState.value = _uiState.value.copy(images = images)
            scheduleAutoSave()
        }
    }

    fun removeImage(index: Int) {
        val images = _uiState.value.images.toMutableList()
        if (index < images.size) {
            val removed = images.removeAt(index)
            if (removed.filePath.isNotEmpty()) {
                val file = getImageFile(removed.filePath)
                file.delete()
            }
            _uiState.value = _uiState.value.copy(images = images)
            scheduleAutoSave()
        }
    }

    fun getImageFile(relativePath: String): File {
        val dir = File(appContext.filesDir, "note_images")
        return File(dir, relativePath)
    }

    private fun saveImageToInternal(uri: Uri): String? {
        return try {
            val dir = File(appContext.filesDir, "note_images")
            dir.mkdirs()
            val fileName = "${UUID.randomUUID()}.jpg"
            val outFile = File(dir, fileName)

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, options)

                val maxDim = 1920
                var sampleSize = 1
                while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
                    sampleSize *= 2
                }

                appContext.contentResolver.openInputStream(uri)?.use { input2 ->
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val bitmap = BitmapFactory.decodeStream(input2, null, decodeOptions)
                    bitmap?.let {
                        FileOutputStream(outFile).use { fos ->
                            it.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                        }
                        it.recycle()
                    }
                }
            }
            fileName
        } catch (_: Exception) {
            null
        }
    }

    fun applyTemplate(title: String, type: NoteType, items: List<String>) {
        _uiState.value = _uiState.value.copy(
            title = title,
            type = type,
            content = if (type == NoteType.TEXT) items.joinToString("\n") else "",
            checklistItems = if (type == NoteType.CHECKLIST) {
                items.map { ChecklistItemUiState(text = it) } + ChecklistItemUiState()
            } else emptyList()
        )
    }

    fun onExtractedText(text: String) {
        val state = _uiState.value
        val lines = com.smarttoolkit.app.feature.notepad.smart.ImageTextExtractor.splitIntoItems(text)
        if (state.type == NoteType.CHECKLIST) {
            val items = state.checklistItems.toMutableList()
            val insertBefore = items.indexOfLast { it.text.isBlank() }.takeIf { it >= 0 } ?: items.size
            lines.forEach { line ->
                items.add(insertBefore, ChecklistItemUiState(text = line))
            }
            _uiState.value = state.copy(checklistItems = items)
        } else {
            val separator = if (state.content.isNotBlank()) "\n" else ""
            _uiState.value = state.copy(content = state.content + separator + lines.joinToString("\n"))
        }
        scheduleAutoSave()
    }

    fun addSuggestedItem(text: String) {
        val items = _uiState.value.checklistItems.toMutableList()
        val lastEmpty = items.indexOfLast { it.text.isBlank() }
        val newItem = ChecklistItemUiState(text = text)
        if (lastEmpty >= 0) {
            items.add(lastEmpty, newItem)
        } else {
            items.add(newItem)
        }
        _uiState.value = _uiState.value.copy(checklistItems = items)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000)
            save()
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val hasContent = state.title.isNotBlank() ||
                state.content.isNotBlank() ||
                state.checklistItems.any { it.text.isNotBlank() }
            if (!hasContent) return@launch

            val note = Note(
                id = if (savedNoteId > 0) savedNoteId else 0,
                title = state.title,
                content = state.content,
                type = state.type,
                category = state.category,
                isPinned = state.isPinned,
                checklistItems = state.checklistItems.mapIndexed { index, item ->
                    ChecklistItem(text = item.text, isChecked = item.isChecked, position = index)
                },
                images = state.images.mapIndexed { index, img ->
                    NoteImage(id = img.id, filePath = img.filePath, position = index)
                },
                createdAt = if (savedNoteId > 0) state.let { System.currentTimeMillis() } else System.currentTimeMillis()
            )

            val id = repository.saveNote(note)
            if (savedNoteId <= 0) {
                savedNoteId = id
                _uiState.value = state.copy(isNew = false)
            }

            // Save image associations for new images
            val currentImages = state.images
            for ((index, img) in currentImages.withIndex()) {
                if (img.isNew && img.id == 0L) {
                    val imageId = repository.addImage(savedNoteId, img.filePath, index)
                    val updated = _uiState.value.images.toMutableList()
                    val imgIndex = updated.indexOfFirst { it.tempEquals(img) }
                    if (imgIndex >= 0) {
                        updated[imgIndex] = updated[imgIndex].copy(id = imageId, isNew = false)
                        _uiState.value = _uiState.value.copy(images = updated)
                    }
                }
            }
        }
    }

    private fun NoteImageUiState.tempEquals(other: NoteImageUiState): Boolean {
        return filePath == other.filePath
    }
}
