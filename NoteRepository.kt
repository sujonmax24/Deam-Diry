package com.sujonmax.diary.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao, private val folderDao: FolderDao) {
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val deletedNotes: Flow<List<Note>> = noteDao.getDeletedNotes()
    val hiddenNotes: Flow<List<Note>> = noteDao.getHiddenNotes()
    val folders: Flow<List<Folder>> = folderDao.getAllFolders()

    fun getNotesByFolder(folderId: Int) = noteDao.getNotesByFolder(folderId)
    suspend fun getNoteById(id: Int) = noteDao.getNoteById(id)
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun softDeleteNote(id: Int) = noteDao.softDeleteNote(id)
    suspend fun restoreNote(id: Int) = noteDao.restoreNote(id)
    suspend fun permanentDeleteNote(id: Int) = noteDao.permanentDeleteNote(id)
    suspend fun setHidden(id: Int, hidden: Boolean) = noteDao.setHidden(id, hidden)
    suspend fun insertFolder(folder: Folder) = folderDao.insertFolder(folder)
    suspend fun deleteFolder(id: Int) = folderDao.deleteFolder(id)
}
