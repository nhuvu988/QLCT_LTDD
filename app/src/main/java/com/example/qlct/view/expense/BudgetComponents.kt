package com.example.qlct.view.expense

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
import com.example.qlct.model.summary.BudgetLevel
import com.example.qlct.model.summary.BudgetStatus
import com.example.qlct.model.validation.AmountValidator
import com.example.qlct.view.components.DashboardCard
import com.example.qlct.view.components.formatMoney
import java.time.YearMonth

/**
 * Hiển thị số đã chi, hạn mức, còn lại/vượt mức và màu cảnh báo theo BudgetStatus.
 * Nút chỉnh sửa chỉ phát sự kiện onEdit; việc lưu ngân sách do ViewModel đảm nhiệm.
 */
@Composable
fun MonthlyBudgetCard(month: YearMonth, status: BudgetStatus, busy: Boolean, onEdit: () -> Unit) {
    DashboardCard("Ngân sách tháng ${month.monthValue}/${month.year}") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Đã chi: ${formatMoney(status.spent)}", style = MaterialTheme.typography.titleMedium)
            if (status.limit == null) {
                Text("Chưa đặt ngân sách cho tháng này.")
                LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.outlineVariant)
            } else {
                Text("Hạn mức: ${formatMoney(status.limit)}")
                // Màu đỏ cho hết/vượt hạn mức, vàng từ 80%, còn lại dùng màu chính.
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

/**
 * Hộp thoại thêm/sửa hạn mức; kiểm tra số tiền trước khi gọi onSave.
 * Chỉ có nút xóa khi đã tồn tại ngân sách và yêu cầu xác nhận trước khi gọi onDelete.
 */
@Composable
fun BudgetEditor(month: String, initialAmount: Long?, busy: Boolean, actionError: String?,
    onDismiss: () -> Unit, onSave: (Long) -> Unit, onDelete: () -> Unit) {
    var amount by rememberSaveable { mutableStateOf(initialAmount?.toString() ?: "") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    // Chỉ bắt đầu hiển thị validation sau khi người dùng nhấn Lưu.
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
    // Hộp thoại xác nhận riêng trước khi xóa hạn mức tháng.
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
