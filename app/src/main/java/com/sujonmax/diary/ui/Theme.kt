package com.sujonmax.diary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DreamDiryTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val blue = Color(0xFF12304A)
    val gold = Color(0xFFD5A84B)
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = gold, secondary = gold, surface = Color(0xFF102334))
        else lightColorScheme(primary = blue, secondary = Color(0xFF9A6B16), surface = Color(0xFFFFFBF5)),
        content = content
    )
}