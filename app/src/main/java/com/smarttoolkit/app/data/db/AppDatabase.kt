package com.smarttoolkit.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, ChecklistItemEntity::class, NoteImageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun noteImageDao(): NoteImageDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN colorLabel TEXT")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN type TEXT NOT NULL DEFAULT 'TEXT'")
                db.execSQL("ALTER TABLE notes ADD COLUMN category TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS checklist_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "noteId INTEGER NOT NULL, " +
                    "text TEXT NOT NULL DEFAULT '', " +
                    "isChecked INTEGER NOT NULL DEFAULT 0, " +
                    "position INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY (noteId) REFERENCES notes(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_checklist_items_noteId ON checklist_items(noteId)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS note_images (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "noteId INTEGER NOT NULL, " +
                    "filePath TEXT NOT NULL, " +
                    "position INTEGER NOT NULL DEFAULT 0, " +
                    "addedAt INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY (noteId) REFERENCES notes(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_images_noteId ON note_images(noteId)")
            }
        }
    }
}
