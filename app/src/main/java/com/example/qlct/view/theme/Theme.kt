package com.example.qlct.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun QlctTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else
                lightColorScheme(primary = Color(0xFF7560C6), secondary = Color(0xFF9C8BD9),
                    background = Color(0xFFF6F5FA), surface = Color.White,
                    surfaceVariant = Color(0xFFF0EDF8), onSurface = Color(0xFF29263A))) {
        content()
    }
}
