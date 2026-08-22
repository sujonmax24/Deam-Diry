package com.sujonmax.diary

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.Update
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* ==================== MainActivity ==================== */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaryNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiaryApp()
                }
            }
        }
    }
}

/* ==================== Theme ==================== */
@Composable
fun DiaryNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF8B5E3C),
            secondary = Color(0xFFD4A574),
            tertiary = Color(0xFF6B8E23),
            background = Color(0xFFF5E6D3),
            surface = Color(0xFFFFF8F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF3E2723),
            onSurface = Color(0xFF3E2723)
        ),
        content = content
    )
}

/* ==================== Settings Manager (সেটিংস সেভ রাখার জন্য) ==================== */
class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("diary_settings", Context.MODE_PRIVATE)

    fun getBrightness(): Float = prefs.getFloat("brightness", 1.0f)
    fun saveBrightness(v: Float) { prefs.edit().putFloat("brightness", v).apply() }

    fun getFontScale(): Float = prefs.getFloat("font_scale", 1.0f)
    fun saveFontScale(v: Float) { prefs.edit().putFloat("font_scale", v).apply() }
}

/* ==================== Database ==================== */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val photoUri: String? = null,
    val folderId: Int? = null,
    val isDeleted: Boolean = false,
    val isHidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isHidden = 0 ORDER BY updatedAt DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isHidden = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getHiddenNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteNote(id: Int)

    @Query("UPDATE notes SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreNote(id: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun permanentDeleteNote(id: Int)

    @Query("UPDATE notes SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: Int, hidden: Boolean)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: Int)
}

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
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

class NoteRepository(private val noteDao: NoteDao, private val folderDao: FolderDao) {
    val activeNotes = noteDao.getActiveNotes()
    val deletedNotes = noteDao.getDeletedNotes()
    val hiddenNotes = noteDao.getHiddenNotes()
    val folders = folderDao.getAllFolders()

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

/* ==================== ViewModel ==================== */
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = NoteDatabase.getDatabase(application)
    private val repository = NoteRepository(db.noteDao(), db.folderDao())

    val activeNotes = repository.activeNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deletedNotes = repository.deletedNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val hiddenNotes = repository.hiddenNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val folders = repository.folders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote

    fun loadNote(id: Int) {
        viewModelScope.launch { _currentNote.value = repository.getNoteById(id) }
    }

    fun saveNote(note: Note, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            if (note.id == 0) repository.insertNote(note)
            else repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            onSaved()
        }
    }

    fun deleteNote(id: Int) { viewModelScope.launch { repository.softDeleteNote(id) } }
    fun restoreNote(id: Int) { viewModelScope.launch { repository.restoreNote(id) } }
    fun permanentDelete(id: Int) { viewModelScope.launch { repository.permanentDeleteNote(id) } }
    fun hideNote(id: Int, hidden: Boolean) { viewModelScope.launch { repository.setHidden(id, hidden) } }
    fun createFolder(name: String) { viewModelScope.launch { repository.insertFolder(Folder(name = name)) } }
    fun deleteFolder(id: Int) { viewModelScope.launch { repository.deleteFolder(id) } }
}

/* ==================== Navigation + Settings Apply ==================== */
@Composable
fun DiaryApp(viewModel: NoteViewModel = viewModel()) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    var brightness by remember { mutableStateOf(settings.getBrightness()) }
    var fontScale by remember { mutableStateOf(settings.getFontScale()) }
    val navController = rememberNavController()
    var isLocked by remember { mutableStateOf(true) }

    // ☀️ Brightness অ্যাপ্লাই করা
    LaunchedEffect(brightness) {
        (context as? Activity)?.window?.let { window ->
            val attrs = window.attributes
            attrs.screenBrightness = brightness
            window.attributes = attrs
        }
    }

    // 🔤 Font Size অ্যাপ্লাই করা
    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, fontScale)
    ) {
        if (isLocked) {
            LockScreen(onUnlocked = { isLocked = false })
        } else {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNoteClick = { navController.navigate("edit/$it") },
                        onAddNote = { navController.navigate("edit/0") },
                        onRecycleBin = { navController.navigate("recycle") },
                        onFolders = { navController.navigate("folders") },
                        onHidden = { navController.navigate("hidden") },
                        onSettings = { navController.navigate("settings") },
                        onAbout = { navController.navigate("about") }
                    )
                }
                composable("edit/{noteId}", arguments = listOf(navArgument("noteId") { type = NavType.IntType })) { back ->
                    val noteId = back.arguments?.getInt("noteId") ?: 0
                    NoteEditorScreen(
                        viewModel = viewModel,
                        noteId = if (noteId == 0) null else noteId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("recycle") { RecycleBinScreen(viewModel, { navController.popBackStack() }) }
                composable("folders") { FolderScreen(viewModel, { navController.popBackStack() }) }
                composable("hidden") { HiddenScreen(viewModel, { navController.popBackStack() }, { navController.navigate("edit/$it") }) }
                composable("settings") {
                    SettingsScreen(
                        brightness = brightness,
                        fontScale = fontScale,
                        onBrightnessChange = { brightness = it; settings.saveBrightness(it) },
                        onFontScaleChange = { fontScale = it; settings.saveFontScale(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("about") { AboutScreen { navController.popBackStack() } }
            }
        }
    }
}

/* ==================== Lock Screen ==================== */
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val correctPin = "1234" // ← আপনার পাসওয়ার্ড এখানে বদলান

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("ডায়রি লক করা আছে 🔒", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4) { pin = it; error = false } },
            placeholder = { Text("PIN দিন (• • • •)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            isError = error,
            supportingText = { if (error) Text("ভুল PIN!", color = Color.Red) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { if (pin == correctPin) onUnlocked() else error = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) { Text("আনলক করুন", modifier = Modifier.padding(8.dp)) }
    }
}

/* ==================== Home Screen ==================== */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onRecycleBin: () -> Unit,
    onFolders: () -> Unit,
    onHidden: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    val notes by viewModel.activeNotes.collectAsState()
    val pagerState = rememberPagerState(pageCount = { notes.size + 1 })
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📔 আমার ডায়রি", fontFamily = FontFamily.Serif) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "মেনু") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("⚙️ Settings") }, onClick = { showMenu = false; onSettings() })
                        DropdownMenuItem(text = { Text("📁 ফোল্ডার তৈরি") }, onClick = { showMenu = false; onFolders() })
                        DropdownMenuItem(text = { Text("🗑️ রিসাইকেল বিন") }, onClick = { showMenu = false; onRecycleBin() })
                        DropdownMenuItem(text = { Text("🙈 লুকানো নোট") }, onClick = { showMenu = false; onHidden() })
                        DropdownMenuItem(text = { Text("ℹ️ About") }, onClick = { showMenu = false; onAbout() })
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNote,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("নতুন পাতা") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page < notes.size) {
                    DiaryPageCard(notes[page]) { onNoteClick(notes[page].id) }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("➕", fontSize = 60.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "নতুন পাতা যোগ করতে\nনিচের বাটনে চাপ দিন",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(minOf(notes.size + 1, 20)) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryPageCard(note: Note, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxSize().padding(24.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                note.title.ifEmpty { "শিরোনামহীন" },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(note.updatedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Text(
                note.content.take(500),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f)
            )
            if (note.photoUri != null) {
                AsyncImage(
                    model = note.photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/* ==================== Note Editor ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(viewModel: NoteViewModel, noteId: Int?, onBack: () -> Unit) {
    val note by viewModel.currentNote.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) { if (noteId != null) viewModel.loadNote(noteId) }
    LaunchedEffect(note) {
        note?.let { title = it.title; content = it.content; photoUri = it.photoUri }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "নতুন নোট" else "নোট সম্পাদনা") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Default.PhotoCamera, "ছবি") }
                    if (noteId != null) {
                        IconButton(onClick = { viewModel.hideNote(noteId, true); onBack() }) { Icon(Icons.Default.VisibilityOff, "লুকান") }
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, "ডিলেট", tint = Color.Red) }
                    }
                    IconButton(onClick = {
                        viewModel.saveNote(Note(id = noteId ?: 0, title = title, content = content, photoUri = photoUri)) { onBack() }
                    }) { Icon(Icons.Default.Save, "সেভ") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("শিরোনাম লিখুন...", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif)
            )
            Spacer(Modifier.height(16.dp))
            if (photoUri != null) {
                Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp)) {
                    Box {
                        AsyncImage(photoUri, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(onClick = { photoUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("আপনার কথা লিখুন...", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontSize = 18.sp)
            )
        }
    }

    if (showDeleteDialog && noteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("নোট ডিলেট করবেন?") },
            text = { Text("নোটটি রিসাইকেল বিনে যাবে।") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNote(noteId); showDeleteDialog = false; onBack() }) {
                    Text("ডিলেট", color = Color.Red)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("বাতিল") } }
        )
    }
}

/* ==================== Settings Screen (নতুন!) ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    brightness: Float,
    fontScale: Float,
    onBrightnessChange: (Float) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            /* ----- Brightness Card ----- */
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("☀️ Screen Brightness", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("বর্তমান: ${(brightness * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onBrightnessChange(maxOf(0.1f, brightness - 0.1f)) }) {
                            Icon(Icons.Default.Remove, "কমান", tint = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { onBrightnessChange(it) },
                            valueRange = 0.1f..1f,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onBrightnessChange(minOf(1f, brightness + 0.1f)) }) {
                            Icon(Icons.Default.Add, "বাড়ান", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TextButton(onClick = { onBrightnessChange(1.0f) }) { Text("Reset (ফুল ব্রাইটনেস)") }
                }
            }

            /* ----- Font Size Card ----- */
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("🔤 Font Size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("বর্তমান: ${(fontScale * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    // লাইভ প্রিভিউ
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Text(
                            "আমার ডায়রি — লেখার প্রিভিউ",
                            modifier = Modifier.padding(16.dp),
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onFontScaleChange(maxOf(0.8f, fontScale - 0.1f)) }) {
                            Icon(Icons.Default.Remove, "ছোট করুন", tint = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = fontScale,
                            onValueChange = { onFontScaleChange(it) },
                            valueRange = 0.8f..1.6f,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onFontScaleChange(minOf(1.6f, fontScale + 0.1f)) }) {
                            Icon(Icons.Default.Add, "বড় করুন", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TextButton(onClick = { onFontScaleChange(1.0f) }) { Text("Reset (ডিফল্ট সাইজ)") }
                }
            }

            Text(
                "💡 সেটিংস অটো সেভ হয় — অ্যাপ বন্ধ করলেও থাকবে",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ==================== Recycle Bin ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(viewModel: NoteViewModel, onBack: () -> Unit) {
    val deleted by viewModel.deletedNotes.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("🗑️ রিসাইকেল বিন") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
        )
    }) { padding ->
        if (deleted.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("রিসাইকেল বিন খালি 🎉")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(deleted) { note ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title.ifEmpty { "শিরোনামহীন" }, style = MaterialTheme.typography.titleMedium)
                                Text(note.content.take(100), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.restoreNote(note.id) }) { Icon(Icons.Default.Restore, "রিস্টোর", tint = Color(0xFF4CAF50)) }
                            IconButton(onClick = { viewModel.permanentDelete(note.id) }) { Icon(Icons.Default.DeleteForever, "চিরতরে মুছুন", tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }
}

/* ==================== Folder Screen ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(viewModel: NoteViewModel, onBack: () -> Unit) {
    val folders by viewModel.folders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("📁 ফোল্ডারসমূহ") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
            actions = { IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.CreateNewFolder, "নতুন ফোল্ডার") } }
        )
    }) { padding ->
        if (folders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📂", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("কোনো ফোল্ডার নেই\nউপরের বাটন থেকে তৈরি করুন", textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(folders) { folder ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(folder.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteFolder(folder.id) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("নতুন ফোল্ডার তৈরি করুন") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("ফোল্ডারের নাম") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) { viewModel.createFolder(name); name = ""; showDialog = false }
                }) { Text("তৈরি করুন") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("বাতিল") } }
        )
    }
}

/* ==================== Hidden Notes ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenScreen(viewModel: NoteViewModel, onBack: () -> Unit, onNoteClick: (Int) -> Unit) {
    val hidden by viewModel.hiddenNotes.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("🙈 লুকানো নোট") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
        )
    }) { padding ->
        if (hidden.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("কোনো লুকানো নোট নেই")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(hidden) { note ->
                    Card(onClick = { onNoteClick(note.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title.ifEmpty { "শিরোনামহীন" }, style = MaterialTheme.typography.titleMedium)
                                Text(note.content.take(80), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.hideNote(note.id, false) }) { Icon(Icons.Default.Visibility, "আনহাইড") }
                        }
                    }
                }
            }
        }
    }
}

/* ==================== About Screen ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("About") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) { Text("S", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(24.dp))
            Text("sujonmax", fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Developer & Designer", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📔 আমার ডায়রি", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Version 1.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    Text("এই অ্যাপটি ভালোবাসা দিয়ে তৈরি ❤️", textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sujonmax")))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
            ) { Text("📘 Facebook এ ফলো করুন", modifier = Modifier.padding(8.dp)) }
        }
    }
}
