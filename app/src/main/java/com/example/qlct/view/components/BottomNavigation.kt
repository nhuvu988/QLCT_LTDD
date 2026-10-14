package com.example.qlct.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.view.theme.Muted
import com.example.qlct.view.theme.Purple

@Composable
internal fun BottomNavigation(selected: Int, onSelect: (Int) -> Unit, onAdd: () -> Unit, canAdd: Boolean) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(enabled = canAdd, onClick = onAdd)
                .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Glyph("edit", if (canAdd) Purple else Muted, Modifier.size(24.dp))
                Text("Nhập vào", fontSize = 10.sp, color = if (canAdd) Purple else Muted)
            }
            NavigationButton("calendar", "Lịch", selected == 1) { onSelect(1) }
            NavigationButton("chart", "Báo cáo", selected == 2) { onSelect(2) }
            NavigationButton("wallet", "Ngân sách", selected == 3) { onSelect(3) }
            NavigationButton("home", "Tổng quan", selected == 0) { onSelect(0) }
        }
    }
}

@Composable
private fun NavigationButton(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Glyph(icon, if (selected) Purple else Muted, Modifier.size(20.dp))
        Text(label, fontSize = 10.sp, lineHeight = 14.sp, color = if (selected) Purple else Muted, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// Code-native line icons keep the same visual weight without adding image assets.
