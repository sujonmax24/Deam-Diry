package com.sujonmax.diary.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ডায়রি-স্টাইল ওয়ার্ম কালার
val DiaryPrimary = Color(0xFF8B5E3C)      // ব্রাউন
val DiarySecondary = Color(0xFFD4A574)    // লাইট ব্রাউন
val DiaryBackground = Color(0xFFF5E6D3)   // ক্রিম
val DiarySurface = Color(0xFFFFF8F0)      // অফ-হোয়াইট
val DiaryAccent = Color(0xFF6B8E23)       // অলিভ গ্রিন
val DiaryText = Color(0xFF3E2723)         // ডার্ক ব্রাউন

private val DiaryColorScheme = lightColorScheme(
    primary = DiaryPrimary,
    secondary = DiarySecondary,
    background = DiaryBackground,
    surface = DiarySurface,
    tertiary = DiaryAccent,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DiaryText,
    onSurface = DiaryText
)

@Composable
fun DiaryNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiaryColorScheme,
        typography = Typography(),
        content = content
    )
}
