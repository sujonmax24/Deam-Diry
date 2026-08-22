package com.sujonmax.diary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sujonmax.diary.data.Note
import com.sujonmax.diary.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    noteId: Int?,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onHide: (Int, Boolean) -> Unit
) {
    val note by viewModel.currentNote.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // নোট লোড করা
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            viewModel.loadNote(noteId)
        }
    }

    LaunchedEffect(note) {
        note?.let {
            title = it.title
            content = it.content
            photoUri = it.photoUri
        }
    }

    // ইমেজ পিকার
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null || noteId == 0) "নতুন নোট" else "নোট সম্পাদনা") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "ফিরে যান")
                    }
                },
                actions = {
                    // ছবি যোগ
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoCamera, "ছবি যোগ করুন")
                    }
                    
                    if (noteId != null && noteId > 0) {
                        // লুকানো
                        IconButton(onClick = { 
                            note?.let { onHide(it.id, !it.isHidden) }
                            onBack()
                        }) {
                            Icon(Icons.Default.VisibilityOff, "লুকান")
                        }
                        
                        // ডিলেট
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "ডিলেট", tint = Color.Red)
                        }
                    }
                    
                    // সেভ
                    IconButton(onClick = {
                        val newNote = Note(
                            id = noteId ?: 0,
                            title = title,
                            content = content,
                            photoUri = photoUri
                        )
                        viewModel.saveNote(newNote) {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Save, "সেভ করুন")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // শিরোনাম
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("শিরোনাম লিখুন...", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ছবি প্রিভিউ
            if (photoUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "সংযুক্ত ছবি",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, "ছবি মুছুন", tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // কন্টেন্ট
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("আপনার কথা লিখুন...", fontFamily = FontFamily.Serif) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }
    }

    // ডিলেট ডায়ালগ
    if (showDeleteDialog && noteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("নোট ডিলেট করবেন?") },
            text = { Text("এই নোটটি রিসাইকেল বিনে চলে যাবে।") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(noteId)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("ডিলেট করুন", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
