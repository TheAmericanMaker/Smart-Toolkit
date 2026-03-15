package com.smarttoolkit.app.feature.notepad

import com.smarttoolkit.app.data.model.NoteType

object NoteShareFormatter {

    fun formatForSharing(
        title: String,
        content: String,
        type: NoteType,
        checklistItems: List<ChecklistItemUiState>
    ): String {
        return buildString {
            if (title.isNotBlank()) {
                appendLine(title)
                appendLine()
            }
            when (type) {
                NoteType.TEXT -> append(content)
                NoteType.CHECKLIST -> {
                    checklistItems
                        .filter { it.text.isNotBlank() }
                        .forEach { item ->
                            val box = if (item.isChecked) "☑" else "☐"
                            appendLine("$box ${item.text}")
                        }
                }
            }
        }.trimEnd()
    }

    fun formatAsHtml(
        title: String,
        content: String,
        type: NoteType,
        checklistItems: List<ChecklistItemUiState>
    ): String {
        return buildString {
            if (title.isNotBlank()) {
                append("<h3>$title</h3>")
            }
            when (type) {
                NoteType.TEXT -> {
                    content.lines().forEach { line ->
                        append("<p>$line</p>")
                    }
                }
                NoteType.CHECKLIST -> {
                    append("<ul style=\"list-style-type: none; padding: 0;\">")
                    checklistItems
                        .filter { it.text.isNotBlank() }
                        .forEach { item ->
                            val decoration = if (item.isChecked) "text-decoration: line-through; color: #888;" else ""
                            val check = if (item.isChecked) "☑" else "☐"
                            append("<li style=\"$decoration\">$check ${item.text}</li>")
                        }
                    append("</ul>")
                }
            }
        }
    }
}
