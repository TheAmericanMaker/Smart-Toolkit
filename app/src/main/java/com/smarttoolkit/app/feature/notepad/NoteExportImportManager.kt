package com.smarttoolkit.app.feature.notepad

import android.content.Context
import android.net.Uri
import com.smarttoolkit.app.data.db.NoteImageEntity
import com.smarttoolkit.app.data.model.ChecklistItem
import com.smarttoolkit.app.data.model.Note
import com.smarttoolkit.app.data.model.NoteImage
import com.smarttoolkit.app.data.model.NoteType
import com.smarttoolkit.app.data.repository.NoteRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(val notesImported: Int, val imagesImported: Int)

@Singleton
class NoteExportImportManager @Inject constructor(
    private val repository: NoteRepository
) {

    suspend fun exportNotes(context: Context, uri: Uri) {
        val notes = repository.getAllNotesWithItems()
        val allImages = repository.getAllImages()
        val imageDir = File(context.filesDir, "note_images")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
                // Write notes.json
                val json = buildExportJson(notes, allImages)
                zip.putNextEntry(ZipEntry("notes.json"))
                zip.write(json.toString(2).toByteArray())
                zip.closeEntry()

                // Write image files
                for (image in allImages) {
                    val imageFile = File(imageDir, image.filePath)
                    if (imageFile.exists()) {
                        zip.putNextEntry(ZipEntry("images/${image.filePath}"))
                        imageFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
    }

    suspend fun importNotes(context: Context, uri: Uri): ImportResult {
        var notesImported = 0
        var imagesImported = 0
        val imageDir = File(context.filesDir, "note_images")
        imageDir.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var jsonContent: String? = null
                val imageFiles = mutableMapOf<String, ByteArray>()

                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "notes.json" -> {
                            jsonContent = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        entry.name.startsWith("images/") -> {
                            val fileName = entry.name.removePrefix("images/")
                            imageFiles[fileName] = zip.readBytes()
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }

                if (jsonContent != null) {
                    val result = parseAndImport(context, jsonContent, imageFiles)
                    notesImported = result.notesImported
                    imagesImported = result.imagesImported
                }
            }
        }

        return ImportResult(notesImported, imagesImported)
    }

    private fun buildExportJson(notes: List<Note>, allImages: List<NoteImageEntity>): JSONObject {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val notesArray = JSONArray()
        for (note in notes) {
            val noteJson = JSONObject()
            noteJson.put("title", note.title)
            noteJson.put("content", note.content)
            noteJson.put("type", note.type.name)
            noteJson.put("category", note.category ?: JSONObject.NULL)
            noteJson.put("isPinned", note.isPinned)
            noteJson.put("iconStyle", note.iconStyle)
            noteJson.put("createdAt", note.createdAt)
            noteJson.put("updatedAt", note.updatedAt)

            if (note.checklistItems.isNotEmpty()) {
                val itemsArray = JSONArray()
                for (item in note.checklistItems) {
                    val itemJson = JSONObject()
                    itemJson.put("text", item.text)
                    itemJson.put("isChecked", item.isChecked)
                    itemJson.put("position", item.position)
                    itemJson.put("indentLevel", item.indentLevel)
                    itemsArray.put(itemJson)
                }
                noteJson.put("checklistItems", itemsArray)
            }

            val noteImages = allImages.filter { it.noteId == note.id }
            if (noteImages.isNotEmpty()) {
                val imagesArray = JSONArray()
                for (image in noteImages) {
                    val imageJson = JSONObject()
                    imageJson.put("filename", image.filePath)
                    imageJson.put("position", image.position)
                    imagesArray.put(imageJson)
                }
                noteJson.put("images", imagesArray)
            }

            notesArray.put(noteJson)
        }

        root.put("notes", notesArray)
        return root
    }

    private suspend fun parseAndImport(
        context: Context,
        jsonContent: String,
        imageFiles: Map<String, ByteArray>
    ): ImportResult {
        var notesImported = 0
        var imagesImported = 0
        val imageDir = File(context.filesDir, "note_images")

        val root = JSONObject(jsonContent)
        val notesArray = root.getJSONArray("notes")

        for (i in 0 until notesArray.length()) {
            val noteJson = notesArray.getJSONObject(i)

            val checklistItems = mutableListOf<ChecklistItem>()
            if (noteJson.has("checklistItems")) {
                val itemsArray = noteJson.getJSONArray("checklistItems")
                for (j in 0 until itemsArray.length()) {
                    val itemJson = itemsArray.getJSONObject(j)
                    checklistItems.add(
                        ChecklistItem(
                            text = itemJson.getString("text"),
                            isChecked = itemJson.getBoolean("isChecked"),
                            position = itemJson.getInt("position"),
                            indentLevel = itemJson.optInt("indentLevel", 0)
                        )
                    )
                }
            }

            val type = try {
                NoteType.valueOf(noteJson.getString("type"))
            } catch (_: Exception) {
                NoteType.TEXT
            }

            val note = Note(
                title = noteJson.getString("title"),
                content = noteJson.optString("content", ""),
                type = type,
                category = if (noteJson.isNull("category")) null else noteJson.optString("category"),
                isPinned = noteJson.optBoolean("isPinned", false),
                iconStyle = noteJson.optString("iconStyle", "CHECKBOX"),
                checklistItems = checklistItems,
                createdAt = noteJson.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = noteJson.optLong("updatedAt", System.currentTimeMillis())
            )

            val noteId = repository.importNote(note)
            notesImported++

            // Import images
            if (noteJson.has("images")) {
                val imagesArray = noteJson.getJSONArray("images")
                for (j in 0 until imagesArray.length()) {
                    val imageJson = imagesArray.getJSONObject(j)
                    val filename = imageJson.getString("filename")
                    val position = imageJson.getInt("position")

                    val imageBytes = imageFiles[filename]
                    if (imageBytes != null) {
                        val outFile = File(imageDir, filename)
                        outFile.writeBytes(imageBytes)
                        repository.insertImageEntity(
                            NoteImageEntity(
                                noteId = noteId,
                                filePath = filename,
                                position = position
                            )
                        )
                        imagesImported++
                    }
                }
            }
        }

        return ImportResult(notesImported, imagesImported)
    }
}
