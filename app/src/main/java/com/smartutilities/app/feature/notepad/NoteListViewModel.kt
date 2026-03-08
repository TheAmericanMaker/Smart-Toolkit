package com.smartutilities.app.feature.notepad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartutilities.app.data.db.NoteDao
import com.smartutilities.app.data.db.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {

    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteDao.delete(note) }
    }
}
