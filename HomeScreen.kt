package com.sujonmax.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujonmax.diary.data.Note
import com.sujonmax.diary.ui.theme.DiarySurface
import com.sujonmax.diary.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onRecycleBin: () -> Unit,
    onAbout: () -> Unit,
    onFolders: () -> Unit
) {
    val notes by viewModel.activeNotes.collectAsState()
    val pagerState = rememberPagerState(pageCount = { notes.size + 1 })
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📔 আমার ডায়রি",
                        fontFamily = FontFamily.Serif
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // নেভিগেশন মেনু (উপরে ডান দিকে)
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "মেনু", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📁 ফোল্ডার") },
                            onClick = {
                                showMenu = false
                                onFolders()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🗑️ রিসাইকেল বিন") },
                            onClick = {
                                showMenu = false
                                onRecycleBin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ℹ️ About") },
                            onClick = {
                                showMenu = false
                                onAbout()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNote,
                icon = { Icon(Icons.Default.Add, "নতুন নোট") },
                text = { Text("নতুন পাতা") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ডায়রি পেজ সোয়াইপ
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page < notes.size) {
                    DiaryPageCard(
                        note = notes[page],
                        onClick = { onNoteClick(notes[page].id) }
                    )
                } else {
                    // শেষ পেজ - নতুন নোট যোগ করার hint
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "➕",
                                fontSize = 60.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "নতুন পাতা যোগ করতে\nFAB বাটনে চাপ দিন",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // পেজ ইন্ডিকেটর
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(notes.size + 1) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
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
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DiarySurface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // ডায়রি লাইন ইফেক্ট
            Text(
                text = note.title.ifEmpty { "শিরোনামহীন" },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(note.updatedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Text(
                text = note.content.take(500) + if (note.content.length > 500) "..." else "",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f)
            )
            
            if (note.photoUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    // ছবি দেখানো হবে
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📷 ছবি সংযুক্ত", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
