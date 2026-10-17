package com.example.qlct.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.view.theme.Muted

/**
 * Khung thẻ dùng chung có tiêu đề, phụ đề tùy chọn và nội dung Compose do nơi gọi truyền vào.
 */
@Composable
internal fun DashboardCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                if (subtitle != null) Text(subtitle, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            }
            content()
        }
    }
}
