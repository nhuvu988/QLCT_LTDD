package com.example.qlct.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.viewmodel.state.ExpenseUiState
import com.example.qlct.view.theme.IncomeGreen

/**
 * Hiển thị tháng, tổng thu/chi và chênh lệch; chuyển sự kiện đổi tháng/khoảng thời gian qua callback.
 */
@Composable
internal fun DashboardHeader(state: ExpenseUiState, previous: () -> Unit, next: () -> Unit, monthOnly: (Boolean) -> Unit, allowAllTime: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(previous, Modifier.semantics { contentDescription = "Tháng trước" }) { Text("‹", fontSize = 30.sp) }
            Surface(Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                Text("${state.month.monthValue.toString().padStart(2, '0')}/${state.month.year}",
                    Modifier.padding(12.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            IconButton(next, Modifier.semantics { contentDescription = "Tháng sau" }) { Text("›", fontSize = 30.sp) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryAmount("Chi tiêu", state.summary.expense, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            SummaryAmount("Thu nhập", state.summary.income, IncomeGreen, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Thu − chi: ${formatMoney(state.summary.income - state.summary.expense)}", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            if (allowAllTime) TextButton({ monthOnly(!state.monthOnly) }) { Text(if (state.monthOnly) "Theo tháng" else "Toàn bộ") }
        }
    }
}

/**
 * Vẽ một ô tổng tiền với nhãn và màu được truyền vào; modifier cho phép chia đều chiều rộng.
 */
@Composable
private fun SummaryAmount(label: String, amount: Long, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(formatMoney(amount), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}
