package com.smarttoolkit.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history WHERE featureKey = :featureKey ORDER BY timestamp DESC")
    fun getByFeature(featureKey: String): Flow<List<HistoryEntry>>

    @Insert
    suspend fun insert(entry: HistoryEntry)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM history WHERE featureKey = :featureKey")
    suspend fun clearFeature(featureKey: String)
}
