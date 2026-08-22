package com.sujonmax.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sujonmax.diary.ui.screens.*
import com.sujonmax.diary.ui.theme.DiaryNoteTheme
import com.sujonmax.diary.viewmodel.NoteViewModel

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

@Composable
fun DiaryApp(viewModel: NoteViewModel = viewModel()) {
    val navController = rememberNavController()
    var isLocked by remember { mutableStateOf(true) }

    if (isLocked) {
        LockScreen(onUnlocked = { isLocked = false })
    } else {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNoteClick = { noteId -> navController.navigate("edit/$noteId") },
                    onAddNote = { navController.navigate("edit/0") },
                    onRecycleBin = { navController.navigate("recycle") },
                    onAbout = { navController.navigate("about") },
                    onFolders = { navController.navigate("folders") }
                )
            }
            
            composable(
                "edit/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                NoteEditorScreen(
                    viewModel = viewModel,
                    noteId = if (noteId == 0) null else noteId,
                    onBack = { navController.popBackStack() },
                    onDelete = { id ->
                        viewModel.deleteNote(id)
                        navController.popBackStack()
                    },
                    onHide = { id, hidden ->
                        viewModel.hideNote(id, hidden)
                    }
                )
            }
            
            composable("recycle") {
                RecycleBinScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
