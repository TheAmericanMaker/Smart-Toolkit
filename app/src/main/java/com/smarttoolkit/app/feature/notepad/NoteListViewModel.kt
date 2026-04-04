package com.smarttoolkit.app.feature.notepad

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.db.NoteEntity
import com.smarttoolkit.app.data.model.NoteType
import com.smarttoolkit.app.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val notes: List<NoteEntity> = emptyList(),
    val searchQuery: String = "",
    val filterType: NoteType? = null,
    val pendingDeleteNote: NoteEntity? = null
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val exportImportManager: NoteExportImportManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filterType = MutableStateFlow<NoteType?>(null)
    private val pendingDelete = MutableStateFlow<NoteEntity?>(null)
    private var deleteJob: Job? = null

    val uiState: StateFlow<NoteListUiState> = combine(
        searchQuery,
        filterType,
        pendingDelete
    ) { query, filter, pending ->
        Triple(query, filter, pending)
    }.flatMapLatest { (query, filter, pending) ->
        val notesFlow = when {
            query.isNotBlank() -> repository.searchNotes(query)
            filter != null -> repository.getNotesByType(filter.name)
            else -> repository.getAllNotes()
        }
        combine(notesFlow, MutableStateFlow(pending)) { notes, pendingNote ->
            NoteListUiState(
                notes = if (pendingNote != null) notes.filter { it.id != pendingNote.id } else notes,
                searchQuery = query,
                filterType = filter,
                pendingDeleteNote = pendingNote
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteListUiState())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterTypeChange(type: NoteType?) {
        filterType.value = type
    }

    fun deleteNote(note: NoteEntity) {
        // If there's already a pending delete, commit it immediately
        val previousPending = pendingDelete.value
        deleteJob?.cancel()
        if (previousPending != null) {
            viewModelScope.launch {
                repository.deleteNote(previousPending.id)
            }
        }
        pendingDelete.value = note
        deleteJob = viewModelScope.launch {
            delay(5000)
            repository.deleteNote(note.id)
            pendingDelete.value = null
        }
    }

    fun undoDelete() {
        deleteJob?.cancel()
        deleteJob = null
        pendingDelete.value = null
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.togglePin(note.id, !note.isPinned)
        }
    }

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun exportNotes(uri: Uri) {
        viewModelScope.launch {
            try {
                exportImportManager.exportNotes(appContext, uri)
                _toastMessage.emit("Notes exported successfully")
            } catch (e: Exception) {
                _toastMessage.emit("Export failed: ${e.message}")
            }
        }
    }

    fun importNotes(uri: Uri) {
        viewModelScope.launch {
            try {
                val result = exportImportManager.importNotes(appContext, uri)
                _toastMessage.emit("Imported ${result.notesImported} notes, ${result.imagesImported} images")
            } catch (e: Exception) {
                val message = e.message ?: "The selected backup file is invalid."
                _toastMessage.emit("Import failed: $message")
            }
        }
    }
}
