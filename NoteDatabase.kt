package com.sujonmax.diary.data

import android.content.Context
import androidx.room.*

@Database(entities = [Note::class, Folder::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
