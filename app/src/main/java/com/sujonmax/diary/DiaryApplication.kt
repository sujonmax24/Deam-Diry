package com.sujonmax.diary

import android.app.Application
import com.sujonmax.diary.data.AppDatabase
import com.sujonmax.diary.data.NoteRepository

class DiaryApplication : Application() {
    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { NoteRepository(database.noteDao(), database.folderDao()) }
}