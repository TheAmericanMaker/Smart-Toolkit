package com.smarttoolkit.app.data.repository

import com.smarttoolkit.app.data.db.ChecklistItemDao
import com.smarttoolkit.app.data.db.ChecklistItemEntity
import com.smarttoolkit.app.data.db.NoteDao
import com.smarttoolkit.app.data.db.NoteEntity
import com.smarttoolkit.app.data.db.NoteImageDao
import com.smarttoolkit.app.data.db.NoteImageEntity
import com.smarttoolkit.app.data.model.ChecklistItem
import com.smarttoolkit.app.data.model.Note
import com.smarttoolkit.app.data.model.NoteImage
import com.smarttoolkit.app.data.model.NoteType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val checklistItemDao: ChecklistItemDao,
    private val noteImageDao: NoteImageDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getNotesByType(type: String): Flow<List<NoteEntity>> = noteDao.getNotesByType(type)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getNoteWithItems(noteId: Long): Note? {
        val entity = noteDao.getNoteById(noteId) ?: return null
        val checklistItems = if (entity.type == "CHECKLIST") {
            checklistItemDao.getItemsForNoteOnce(noteId).map { it.toDomain() }
        } else {
            emptyList()
        }
        val images = noteImageDao.getImagesForNoteOnce(noteId).map { it.toDomain() }
        return entity.toDomain(checklistItems, images)
    }

    suspend fun saveNote(note: Note): Long {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            type = note.type.name,
            category = note.category,
            colorLabel = note.colorLabel,
            isPinned = note.isPinned,
            iconStyle = note.iconStyle,
            createdAt = note.createdAt,
            updatedAt = now
        )

        val noteId = if (note.id == 0L) {
            noteDao.insert(entity)
        } else {
            noteDao.update(entity)
            note.id
        }

        if (note.type == NoteType.CHECKLIST) {
            checklistItemDao.deleteAllForNote(noteId)
            val itemEntities = note.checklistItems.mapIndexed { index, item ->
                ChecklistItemEntity(
                    noteId = noteId,
                    text = item.text,
                    isChecked = item.isChecked,
                    position = index,
                    indentLevel = item.indentLevel
                )
            }
            checklistItemDao.insertAll(itemEntities)
        }

        return noteId
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteById(noteId)
    }

    suspend fun togglePin(noteId: Long, pinned: Boolean) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.update(note.copy(isPinned = pinned))
    }

    suspend fun getAllNotesWithItems(): List<Note> {
        val notes = noteDao.getAllNotesOnce()
        return notes.map { entity ->
            val items = if (entity.type == "CHECKLIST") {
                checklistItemDao.getItemsForNoteOnce(entity.id).map { it.toDomain() }
            } else emptyList()
            val images = noteImageDao.getImagesForNoteOnce(entity.id).map { it.toDomain() }
            entity.toDomain(items, images)
        }
    }

    suspend fun importNote(note: Note): Long {
        val entity = NoteEntity(
            title = note.title,
            content = note.content,
            type = note.type.name,
            category = note.category,
            colorLabel = note.colorLabel,
            isPinned = note.isPinned,
            iconStyle = note.iconStyle,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
        val noteId = noteDao.insert(entity)

        if (note.checklistItems.isNotEmpty()) {
            val items = note.checklistItems.mapIndexed { index, item ->
                ChecklistItemEntity(
                    noteId = noteId,
                    text = item.text,
                    isChecked = item.isChecked,
                    position = index,
                    indentLevel = item.indentLevel
                )
            }
            checklistItemDao.insertAll(items)
        }

        return noteId
    }

    fun getChecklistItemsFlow(noteId: Long): Flow<List<ChecklistItemEntity>> {
        return checklistItemDao.getItemsForNote(noteId)
    }

    fun getImagesFlow(noteId: Long): Flow<List<NoteImageEntity>> {
        return noteImageDao.getImagesForNote(noteId)
    }

    suspend fun addImage(noteId: Long, filePath: String, position: Int): Long {
        return noteImageDao.insert(
            NoteImageEntity(noteId = noteId, filePath = filePath, position = position)
        )
    }

    suspend fun deleteImage(imageId: Long) {
        noteImageDao.deleteById(imageId)
    }

    suspend fun getImagesForNote(noteId: Long): List<NoteImageEntity> {
        return noteImageDao.getImagesForNoteOnce(noteId)
    }

    suspend fun getAllImages(): List<NoteImageEntity> {
        return noteImageDao.getAllImages()
    }

    suspend fun insertImageEntity(image: NoteImageEntity): Long {
        return noteImageDao.insert(image)
    }
}

private fun NoteEntity.toDomain(
    checklistItems: List<ChecklistItem> = emptyList(),
    images: List<NoteImage> = emptyList()
): Note = Note(
    id = id,
    title = title,
    content = content,
    type = try { NoteType.valueOf(type) } catch (_: Exception) { NoteType.TEXT },
    category = category,
    colorLabel = colorLabel,
    isPinned = isPinned,
    iconStyle = iconStyle,
    checklistItems = checklistItems,
    images = images,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun ChecklistItemEntity.toDomain(): ChecklistItem = ChecklistItem(
    id = id,
    text = text,
    isChecked = isChecked,
    position = position,
    indentLevel = indentLevel
)

private fun NoteImageEntity.toDomain(): NoteImage = NoteImage(
    id = id,
    filePath = filePath,
    position = position,
    addedAt = addedAt
)
