package com.sujonmax.diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sujonmax.diary.data.*
import com.sujonmax.diary.security.PinStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as DiaryApplication
        val vm = ViewModelProvider(this, DiaryViewModel.factory(app.repository))[DiaryViewModel::class.java]
        setContent { DreamDiryTheme { DiaryApp(vm, PinStore(this)) } }
    }
}

class DiaryViewModel(private val repository: NoteRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val search = query.asStateFlow()
    val notes = query.flatMapLatest(repository::observeNotes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val trash = repository.trash.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun setQuery(value: String) { query.value = value }
    fun save(note: NoteEntity, done: () -> Unit = {}) = viewModelScope.launch { repository.save(note); done() }
    fun trash(id: Long) = viewModelScope.launch { repository.trash(id) }
    fun restore(id: Long) = viewModelScope.launch { repository.restore(id) }
    fun deleteForever(id: Long) = viewModelScope.launch { repository.deleteForever(id) }
    companion object {
        fun factory(repo: NoteRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DiaryViewModel(repo) as T
        }
    }
}

@Composable
private fun DiaryApp(vm: DiaryViewModel, pinStore: PinStore) {
    var unlocked by remember { mutableStateOf(!pinStore.configured) }
    var trashOpen by remember { mutableStateOf(false) }
    var editorNote by remember { mutableStateOf<NoteEntity?>(null) }
    if (!unlocked) {
        PinScreen(pinStore) { unlocked = true }
        return
    }
    when {
        trashOpen -> TrashScreen(vm, onBack = { trashOpen = false })
        editorNote != null -> EditorScreen(editorNote!!, vm) { editorNote = null }
        else -> HomeScreen(vm, onTrash = { trashOpen = true }, onEdit = { editorNote = it },
            onNew = { editorNote = NoteEntity() })
    }
}

@Composable
private fun PinScreen(store: PinStore, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var setup by remember { mutableStateOf(!store.configured) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(if (setup) "Create your PIN" else "Dream Diry is locked", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(pin, { if (it.length <= 8 && it.all(Char::isDigit)) pin = it },
            label = { Text(if (setup) "New PIN" else "PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        if (setup) OutlinedTextField(confirmation, { confirmation = it }, label = { Text("Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button({
            if (setup && pin == confirmation && pin.length >= 4) { store.setPin(pin); onUnlocked() }
            else if (!setup && store.verify(pin)) onUnlocked()
            else error = if (setup) "PINs must match and contain 4–8 digits" else "Incorrect PIN or temporary lockout"
        }, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text(if (setup) "Save PIN" else "Unlock") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(vm: DiaryViewModel, onTrash: () -> Unit, onEdit: (NoteEntity) -> Unit, onNew: () -> Unit) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    val search by vm.search.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar({ Text("Dream Diry") }, actions = {
        IconButton(onClick = onTrash) { Icon(Icons.Default.Delete, "Recycle bin") }
    }) }, floatingActionButton = { FloatingActionButton(onNew) { Icon(Icons.Default.Add, "New note") } }) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp)) {
            OutlinedTextField(search, vm::setQuery, label = { Text("Search notes") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
                items(notes, key = { it.id }) { note ->
                    Card(onClick = { onEdit(note) }, Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium)
                            Text(note.content.take(180), maxLines = 3)
                            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(note.updatedAt)),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(initial: NoteEntity, vm: DiaryViewModel, onBack: () -> Unit) {
    var title by remember(initial.id) { mutableStateOf(initial.title) }
    var content by remember(initial.id) { mutableStateOf(initial.content) }
    var tags by remember(initial.id) { mutableStateOf(initial.tags) }
    Scaffold(topBar = { TopAppBar({ Text(if (initial.id == 0L) "New note" else "Edit note") },
        navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
        actions = { IconButton({ vm.save(initial.copy(title = title, content = content, tags = tags)); onBack() }) {
            Icon(Icons.Default.Save, "Save")
        } }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, Modifier.fillMaxWidth())
            OutlinedTextField(content, { content = it }, label = { Text("Write your diary…") },
                Modifier.fillMaxWidth().weight(1f), minLines = 12)
            OutlinedTextField(tags, { tags = it }, label = { Text("Tags (comma separated)") }, singleLine = true, Modifier.fillMaxWidth())
            if (initial.id != 0L) TextButton({ vm.trash(initial.id); onBack() }) { Text("Move to recycle bin") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashScreen(vm: DiaryViewModel, onBack: () -> Unit) {
    val notes by vm.trash.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar({ Text("Recycle bin") }, navigationIcon = { IconButton(onBack) {
        Icon(Icons.Default.ArrowBack, "Back")
    } }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.id }) { note -> Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(note.title.ifBlank { "Untitled" }, Modifier.weight(1f))
                    TextButton({ vm.restore(note.id) }) { Text("Restore") }
                    IconButton({ vm.deleteForever(note.id) }) { Icon(Icons.Default.DeleteForever, "Delete permanently") }
                }
            } }
        }
    }
}