package com.smarttoolkit.app.di

import android.content.Context
import androidx.room.Room
import com.smarttoolkit.app.data.db.AppDatabase
import com.smarttoolkit.app.data.db.ChecklistItemDao
import com.smarttoolkit.app.data.db.NoteDao
import com.smarttoolkit.app.data.db.NoteImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_toolkit.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideChecklistItemDao(database: AppDatabase): ChecklistItemDao {
        return database.checklistItemDao()
    }

    @Provides
    fun provideNoteImageDao(database: AppDatabase): NoteImageDao {
        return database.noteImageDao()
    }
}
