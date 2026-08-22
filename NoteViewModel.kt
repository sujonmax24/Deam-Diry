package com.sujonmax.diary.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.sujonmax.diary.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NoteDatabase.getDatabase(application)
    private val repository = NoteRepository(database.noteDao(), database.folderDao())

    val activeNotes = repository.activeNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val deletedNotes = repository.deletedNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val folders = repository.folders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote

    fun loadNote(id: Int) {
        viewModelScope.launch {
            _currentNote.value = repository.getNoteById(id)
        }
    }

    fun saveNote(note: Note, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (note.id == 0) {
                val id = repository.insertNote(note)
                onSaved(id)
            } else {
                repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
                onSaved(note.id.toLong())
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch { repository.softDeleteNote(id) }
    }

    fun restoreNote(id: Int) {
        viewModelScope.launch { repository.restoreNote(id) }
    }

    fun permanentDelete(id: Int) {
        viewModelScope.launch { repository.permanentDeleteNote(id) }
    }

    fun hideNote(id: Int, hidden: Boolean) {
        viewModelScope.launch { repository.setHidden(id, hidden) }
    }

    fun createFolder(name: String) {
        viewModelScope.launch { repository.insertFolder(Folder(name = name)) }
    }

    fun deleteFolder(id: Int) {
        viewModelScope.launch { repository.deleteFolder(id) }
    }
}
