package com.smarttoolkit.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteImageDao {
    @Query("SELECT * FROM note_images WHERE noteId = :noteId ORDER BY position ASC")
    fun getImagesForNote(noteId: Long): Flow<List<NoteImageEntity>>

    @Query("SELECT * FROM note_images WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getImagesForNoteOnce(noteId: Long): List<NoteImageEntity>

    @Insert
    suspend fun insert(image: NoteImageEntity): Long

    @Insert
    suspend fun insertAll(images: List<NoteImageEntity>)

    @Query("DELETE FROM note_images WHERE id = :imageId")
    suspend fun deleteById(imageId: Long)

    @Query("DELETE FROM note_images WHERE noteId = :noteId")
    suspend fun deleteAllForNote(noteId: Long)

    @Query("SELECT * FROM note_images")
    suspend fun getAllImages(): List<NoteImageEntity>
}
