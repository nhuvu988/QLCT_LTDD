package com.example.qlct.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun QlctTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme(
        primary = Color(0xFFCDBBFF), onPrimary = Color(0xFF34215F),
        primaryContainer = Color(0xFF493673), onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DE), background = Color(0xFF141119),
        onBackground = Color(0xFFEAE1F0), surface = Color(0xFF1E1925),
        onSurface = Color(0xFFEAE1F0), surfaceVariant = Color(0xFF39323F),
        onSurfaceVariant = Color(0xFFCEC3D5), outline = Color(0xFF998DA3),
        outlineVariant = Color(0xFF514657)
    ) else
                lightColorScheme(primary = Color(0xFF7560C6), secondary = Color(0xFF9C8BD9),
                    background = Color(0xFFF6F5FA), surface = Color.White,
                    surfaceVariant = Color(0xFFF0EDF8), onSurface = Color(0xFF29263A))) {
        content()
    }
}
