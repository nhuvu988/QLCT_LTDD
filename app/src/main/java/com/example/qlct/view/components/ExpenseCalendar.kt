package com.example.qlct.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.model.entity.TransactionRow
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun ExpenseCalendar(month: YearMonth, rows: List<TransactionRow>, selected: String?, onSelect: (String?) -> Unit) {
    val start = month.atDay(1).dayOfWeek.value - 1
    val count = ((start + month.lengthOfMonth() + 6) / 7) * 7
    val dates = rows.map { it.date }.toSet()
    val today = LocalDate.now().toString()
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEachIndexed { i, label ->
                    Text(label, Modifier.weight(1f).padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (i >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp)
                }
            }
            (0 until count).chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { cell ->
                        val day = cell - start + 1
                        if (day !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(48.dp))
                        else {
                            val date = month.atDay(day).toString()
                            val active = selected == date
                            Surface(onClick = { onSelect(if (active) null else date) },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(8.dp),
                                color = if (active) MaterialTheme.colorScheme.primary else if (date == today) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) {
                                Column(Modifier.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(day.toString(), fontSize = 14.sp, fontWeight = if (date == today || active) FontWeight.Bold else FontWeight.Normal)
                                    Box(Modifier.size(4.dp).background(
                                        if (date in dates) { if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary }
                                        else androidx.compose.ui.graphics.Color.Transparent, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
            TextButton(onClick = { onSelect(null) }, modifier = Modifier.align(Alignment.End)) {
                Text(if (selected == null) "Chọn ngày để xem giao dịch" else "Xem cả tháng")
            }
        }
    }
}
