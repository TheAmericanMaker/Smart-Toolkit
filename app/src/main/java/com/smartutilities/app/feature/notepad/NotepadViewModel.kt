package com.smartutilities.app.feature.notepad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartutilities.app.data.db.NoteDao
import com.smartutilities.app.data.db.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditUiState(
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true
)

@HiltViewModel
class NotepadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteDao: NoteDao
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<String>("noteId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    private var existingNote: NoteEntity? = null

    init {
        if (noteId > 0) {
            viewModelScope.launch {
                noteDao.getNoteById(noteId)?.let { note ->
                    existingNote = note
                    _uiState.value = NoteEditUiState(
                        title = note.title,
                        content = note.content,
                        isNew = false
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun onContentChange(content: String) { _uiState.value = _uiState.value.copy(content = content) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank() && state.content.isBlank()) return@launch

            val existing = existingNote
            if (existing != null) {
                noteDao.update(existing.copy(
                    title = state.title,
                    content = state.content,
                    updatedAt = System.currentTimeMillis()
                ))
            } else {
                noteDao.insert(NoteEntity(
                    title = state.title,
                    content = state.content
                ))
            }
        }
    }
}
