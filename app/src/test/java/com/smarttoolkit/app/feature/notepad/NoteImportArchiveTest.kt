package com.smarttoolkit.app.feature.notepad

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteImportArchiveTest {

    @Test
    fun validArchiveStagesSuccessfully() {
        val archiveBytes = zipBytes(
            "notes.json" to """
                {
                  "notes": [
                    {
                      "title": "Trip",
                      "content": "Pack charger",
                      "type": "TEXT",
                      "images": [
                        { "filename": "photo.jpg", "position": 0 }
                      ]
                    }
                  ]
                }
            """.trimIndent().toByteArray(),
            "images/photo.jpg" to byteArrayOf(1, 2, 3, 4)
        )

        withTempDir { stagingDir ->
            val archive = NoteImportArchive.stageValidatedArchive(
                inputStream = archiveBytes.inputStream(),
                stagingDir = stagingDir
            )

            assertEquals(1, archive.notes.size)
            assertEquals(1, archive.notes.first().images.size)
            assertEquals("photo.jpg", archive.notes.first().images.first().archiveName)
            assertEquals(1, archive.stagedImages.size)
            assertTrue(archive.stagedImages.getValue("photo.jpg").stagedFile.exists())
        }
    }

    @Test
    fun rejectsTraversalPaths() {
        val archiveBytes = zipBytes(
            "notes.json" to """{ "notes": [] }""".toByteArray(),
            "images/../evil.jpg" to byteArrayOf(1)
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup contains invalid file paths.", error.message)
        }
    }

    @Test
    fun rejectsAbsolutePaths() {
        val archiveBytes = zipBytes(
            "/notes.json" to """{ "notes": [] }""".toByteArray()
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup contains invalid file paths.", error.message)
        }
    }

    @Test
    fun rejectsUnexpectedTopLevelFiles() {
        val archiveBytes = zipBytes(
            "notes.json" to """{ "notes": [] }""".toByteArray(),
            "evil.txt" to "nope".toByteArray()
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup contains unsupported files.", error.message)
        }
    }

    @Test
    fun rejectsDuplicateNotesJsonEntries() {
        val archiveBytes = duplicateNotesArchive()

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup contains duplicate notes data.", error.message)
        }
    }

    @Test
    fun rejectsMissingNotesJson() {
        val archiveBytes = zipBytes(
            "images/photo.jpg" to byteArrayOf(1, 2, 3)
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup is missing notes.json.", error.message)
        }
    }

    @Test
    fun rejectsMissingReferencedImages() {
        val archiveBytes = zipBytes(
            "notes.json" to """
                {
                  "notes": [
                    {
                      "title": "Trip",
                      "type": "TEXT",
                      "images": [
                        { "filename": "missing.jpg", "position": 0 }
                      ]
                    }
                  ]
                }
            """.trimIndent().toByteArray()
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup is missing an attached image.", error.message)
        }
    }

    @Test
    fun rejectsOversizedNotesJson() {
        val oversizedJson = buildString {
            append("{ \"notes\": [ { \"title\": \"A\", \"content\": \"")
            append("x".repeat((MAX_IMPORT_NOTES_JSON_BYTES + 1).toInt()))
            append("\" } ] }")
        }.toByteArray()

        val archiveBytes = zipBytes("notes.json" to oversizedJson)

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup notes data is too large.", error.message)
        }
    }

    @Test
    fun rejectsOversizedImageEntries() {
        val archiveBytes = zipBytes(
            "notes.json" to """{ "notes": [] }""".toByteArray(),
            "images/photo.jpg" to ByteArray(MAX_IMPORT_IMAGE_BYTES.toInt() + 1) { 7 }
        )

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup image is too large.", error.message)
        }
    }

    @Test
    fun rejectsTooManyImages() {
        val entries = mutableListOf<Pair<String, ByteArray>>()
        entries += "notes.json" to """{ "notes": [] }""".toByteArray()
        repeat(MAX_IMPORT_IMAGE_COUNT + 1) { index ->
            entries += "images/image_$index.jpg" to byteArrayOf(index.toByte())
        }

        val archiveBytes = zipBytes(*entries.toTypedArray())

        withTempDir { stagingDir ->
            val error = assertThrows(ImportValidationException::class.java) {
                NoteImportArchive.stageValidatedArchive(archiveBytes.inputStream(), stagingDir)
            }
            assertEquals("Backup contains too many images.", error.message)
        }
    }

    @Test
    fun generatesSafeImportedImageNames() {
        val generated = NoteImportArchive.generateImportedFileName("photo.JPG")

        assertTrue(generated.endsWith(".jpg"))
        assertNotEquals("photo.JPG", generated)
    }

    private fun zipBytes(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun duplicateNotesArchive(): ByteArray {
        val archive = zipBytes(
            "notes0json" to """{ "notes": [] }""".toByteArray(),
            "notes1json" to """{ "notes": [] }""".toByteArray()
        )

        return archive
            .replaceAscii("notes0json", "notes.json")
            .replaceAscii("notes1json", "notes.json")
    }

    private fun withTempDir(block: (File) -> Unit) {
        val dir = File.createTempFile("note-import", "")
        dir.delete()
        dir.mkdirs()
        try {
            block(dir)
        } finally {
            dir.deleteRecursively()
        }
    }

    private fun ByteArray.replaceAscii(from: String, to: String): ByteArray {
        require(from.length == to.length)
        val fromBytes = from.encodeToByteArray()
        val toBytes = to.encodeToByteArray()
        var index = 0

        while (index <= size - fromBytes.size) {
            var matches = true
            for (offset in fromBytes.indices) {
                if (this[index + offset] != fromBytes[offset]) {
                    matches = false
                    break
                }
            }

            if (matches) {
                for (offset in toBytes.indices) {
                    this[index + offset] = toBytes[offset]
                }
                index += toBytes.size
            } else {
                index++
            }
        }

        return this
    }
}
