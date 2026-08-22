@Composable
fun DiaryApp(viewModel: NoteViewModel = viewModel()) {
    val navController = rememberNavController()
    var isLocked by remember { mutableStateOf(true) }

    if (isLocked) {
        LockScreen(onUnlocked = { isLocked = false })
    } else {
        NavHost(navController = navController, startDestination = "home") {
            
            // হোম স্ক্রিন
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
            
            // নোট এডিটর
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
            
            // রিসাইকেল বিন
            composable("recycle") {
                RecycleBinScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            // ফোল্ডার
            composable("folders") {
                FolderScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onFolderClick = { folderId ->
                        navController.navigate("folder_notes/$folderId")
                    }
                )
            }
            
            // About
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
