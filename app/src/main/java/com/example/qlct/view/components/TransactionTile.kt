package com.example.qlct.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.view.theme.IncomeGreen
import com.example.qlct.view.theme.Muted
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun TransactionTile(transaction: TransactionRow, busy: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = categoryColor(transaction.categoryId)
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().clickable(enabled = !busy, onClick = onEdit).padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(color.copy(alpha = .16f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Glyph(when (transaction.categoryId) { 1L -> "food"; 2L -> "car"; 4L -> "bag"; 5L -> "home"; else -> "wallet" }, color, Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transaction.categoryName, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(runCatching { LocalDate.parse(transaction.date).format(DateTimeFormatter.ofPattern("dd/MM")) }.getOrDefault(transaction.date) +
                    if (transaction.note.isBlank()) "" else " · ${transaction.note}", fontSize = 10.sp, lineHeight = 14.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${if (transaction.type == "INCOME") "+" else "−"}${formatMoney(transaction.amount)}", fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = if (transaction.type == "INCOME") IncomeGreen else Color(0xFFD17F94))
            IconButton(onClick = onDelete, enabled = !busy, modifier = Modifier.size(40.dp).semantics { contentDescription = "Xóa giao dịch ${transaction.categoryName}" }) {
                Glyph("trash", Muted, Modifier.size(15.dp))
            }
        }
    }
}
