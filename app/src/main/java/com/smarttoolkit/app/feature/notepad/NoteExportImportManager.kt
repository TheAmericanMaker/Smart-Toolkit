package com.smarttoolkit.app.feature.notepad

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.smarttoolkit.app.data.db.AppDatabase
import com.smarttoolkit.app.data.db.NoteImageEntity
import com.smarttoolkit.app.data.model.Note
import com.smarttoolkit.app.data.repository.NoteRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(val notesImported: Int, val imagesImported: Int)

@Singleton
class NoteExportImportManager @Inject constructor(
    private val repository: NoteRepository,
    private val appDatabase: AppDatabase
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
        val stagingDir = File(context.cacheDir, "note_import_${System.currentTimeMillis()}")
        return try {
            val validatedArchive = try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    NoteImportArchive.stageValidatedArchive(inputStream, stagingDir)
                } ?: throw ImportValidationException("Could not read the selected backup file.")
            } catch (e: ImportValidationException) {
                throw e
            } catch (_: IOException) {
                throw ImportValidationException("Could not read the selected backup file.")
            } catch (_: Exception) {
                throw ImportValidationException("Could not read the selected backup file.")
            }
            persistValidatedArchive(context, validatedArchive)
        } finally {
            stagingDir.deleteRecursively()
        }
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

    private suspend fun persistValidatedArchive(
        context: Context,
        archive: ValidatedNoteImportArchive
    ): ImportResult {
        val imageDir = File(context.filesDir, "note_images")
        imageDir.mkdirs()
        val createdFiles = mutableListOf<File>()

        return try {
            appDatabase.withTransaction {
                var notesImported = 0
                var imagesImported = 0

                for (bundle in archive.notes) {
                    val noteId = repository.importNote(bundle.note)
                    notesImported++

                    for (imageRef in bundle.images) {
                        val stagedImage = archive.stagedImages[imageRef.archiveName]
                            ?: throw ImportValidationException("Backup is missing an attached image.")
                        val fileName = NoteImportArchive.generateImportedFileName(imageRef.archiveName)
                        val outFile = File(imageDir, fileName)
                        stagedImage.stagedFile.copyTo(outFile, overwrite = false)
                        createdFiles.add(outFile)
                        repository.insertImageEntity(
                            NoteImageEntity(
                                noteId = noteId,
                                filePath = fileName,
                                position = imageRef.position
                            )
                        )
                        imagesImported++
                    }
                }

                ImportResult(notesImported = notesImported, imagesImported = imagesImported)
            }
        } catch (e: Exception) {
            createdFiles.forEach { it.delete() }
            throw e
        }
    }
}
