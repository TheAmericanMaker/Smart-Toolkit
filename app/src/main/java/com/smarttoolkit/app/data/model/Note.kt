package com.smarttoolkit.app.data.model

enum class NoteType { TEXT, CHECKLIST }

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val type: NoteType = NoteType.TEXT,
    val category: String? = null,
    val isPinned: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val images: List<NoteImage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val id: Long = 0,
    val text: String = "",
    val isChecked: Boolean = false,
    val position: Int = 0
)

data class NoteImage(
    val id: Long = 0,
    val filePath: String = "",
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
