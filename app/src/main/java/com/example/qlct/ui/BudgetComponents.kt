package com.example.qlct.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.qlct.data.AmountValidator
import java.time.YearMonth

@Composable
fun MonthlyBudgetCard(month: YearMonth, status: BudgetStatus, busy: Boolean, onEdit: () -> Unit) {
    DashboardCard("Ngân sách tháng ${month.monthValue}/${month.year}") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Luôn tính riêng tháng này, kể cả khi xem toàn bộ giao dịch.", style = MaterialTheme.typography.bodySmall)
            Text("Đã chi: ${formatMoney(status.spent)}")
            if (status.limit == null) {
                Text("Chưa đặt ngân sách cho tháng này.")
            } else {
                Text("Hạn mức: ${formatMoney(status.limit)}")
                val color = when (status.level) {
                    BudgetLevel.EXCEEDED, BudgetLevel.REACHED -> MaterialTheme.colorScheme.error
                    BudgetLevel.WARNING -> Color(0xFFAD6800)
                    else -> MaterialTheme.colorScheme.primary
                }
                LinearProgressIndicator(progress = { status.progress }, color = color, modifier = Modifier.fillMaxWidth())
                Text(if (status.remaining!! < 0) "Vượt ngân sách: ${formatMoney(-status.remaining!!)}"
                    else "Còn lại: ${formatMoney(status.remaining!!)}", color = color)
                when (status.level) {
                    BudgetLevel.WARNING -> Text("Bạn đã sử dụng từ 80% ngân sách.", color = color)
                    BudgetLevel.REACHED -> Text("Bạn đã dùng hết ngân sách tháng.", color = color)
                    BudgetLevel.EXCEEDED -> Text("Chi tiêu đã vượt hạn mức tháng.", color = color)
                    else -> Unit
                }
            }
            OutlinedButton(onClick = onEdit, enabled = !busy) {
                Text(if (status.limit == null) "Đặt ngân sách" else "Sửa ngân sách")
            }
        }
    }
}

@Composable
fun BudgetEditor(month: String, initialAmount: Long?, busy: Boolean, actionError: String?,
    onDismiss: () -> Unit, onSave: (Long) -> Unit, onDelete: () -> Unit) {
    var amount by rememberSaveable { mutableStateOf(initialAmount?.toString() ?: "") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val error = if (checked) AmountValidator.errorForInput(amount) else null
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Ngân sách $month") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, enabled = !busy,
                    label = { Text("Hạn mức (VND)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null, supportingText = { Text(error ?: "Nhập số nguyên VND lớn hơn 0") })
                actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (initialAmount != null) TextButton(onClick = { confirmDelete = true }, enabled = !busy) { Text("Xóa ngân sách") }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = {
            checked = true
            if (AmountValidator.errorForInput(amount) == null) onSave(amount.trim().toLong())
        }) { Text(if (busy) "Đang lưu…" else "Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Hủy") } }
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { if (!busy) confirmDelete = false },
        title = { Text("Xóa ngân sách $month?") },
        text = { Column {
            Text("Chỉ xóa hạn mức. Các giao dịch được giữ nguyên.")
            actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { TextButton(onClick = onDelete, enabled = !busy) { Text("Xóa") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }, enabled = !busy) { Text("Hủy") } }
    )
}
