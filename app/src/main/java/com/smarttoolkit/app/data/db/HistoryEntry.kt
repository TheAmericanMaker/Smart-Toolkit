package com.smarttoolkit.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val featureKey: String,
    val label: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
