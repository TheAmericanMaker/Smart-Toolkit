package com.smarttoolkit.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {
    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY position ASC")
    fun getItemsForNote(noteId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getItemsForNoteOnce(noteId: Long): List<ChecklistItemEntity>

    @Insert
    suspend fun insert(item: ChecklistItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<ChecklistItemEntity>)

    @Update
    suspend fun update(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :itemId")
    suspend fun setChecked(itemId: Long, checked: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteAllForNote(noteId: Long)
}
