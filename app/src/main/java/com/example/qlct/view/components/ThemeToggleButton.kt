package com.example.qlct.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun ThemeToggleButton(darkTheme: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.semantics {
        contentDescription = if (darkTheme) "Chuyển sang giao diện sáng" else "Chuyển sang giao diện tối"
    }) {
        Glyph(if (darkTheme) "sun" else "moon", Color.White, Modifier.size(24.dp))
    }
}
