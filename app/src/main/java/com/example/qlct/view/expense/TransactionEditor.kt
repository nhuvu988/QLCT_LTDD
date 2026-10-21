package com.example.qlct.view.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.model.validation.AmountValidator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionEditor(
    existing: TransactionRow?, categories: List<Category>, busy: Boolean, actionError: String?,
    onDismiss: () -> Unit, onSave: (TransactionEntity) -> Unit
) {
    var type by rememberSaveable { mutableStateOf(existing?.type ?: "EXPENSE") }
    var amount by rememberSaveable { mutableStateOf(existing?.amount?.toString() ?: "") }
    var categoryId by rememberSaveable { mutableLongStateOf(existing?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var date by rememberSaveable { mutableStateOf(existing?.date ?: LocalDate.now().toString()) }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var validation by rememberSaveable { mutableStateOf<String?>(null) }
    var amountChecked by rememberSaveable { mutableStateOf(false) }
    val amountError = if (amountChecked) AmountValidator.errorForInput(amount) else null
    var categoriesOpen by remember { mutableStateOf(false) }
    var dateOpen by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(if (existing == null) "Thêm giao dịch" else "Sửa giao dịch") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == "EXPENSE", onClick = { type = "EXPENSE" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !busy, label = { Text("Chi tiền") })
                    FilterChip(type == "INCOME", onClick = { type = "INCOME" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !busy, label = { Text("Thu tiền") })
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it; validation = null }, label = { Text("Số tiền (VND)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !busy,
                    isError = amountError != null,
                    supportingText = { Text(amountError ?: "Nhập số tiền lớn hơn 0; chọn Thu hoặc Chi ở trên") },
                    modifier = Modifier.fillMaxWidth())
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { categoriesOpen = true }, enabled = !busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))) {
                        Text("Danh mục: ${categories.find { it.id == categoryId }?.name ?: "Chọn danh mục"}")
                    }
                    DropdownMenu(expanded = categoriesOpen, onDismissRequest = { categoriesOpen = false }) {
                        categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { categoryId = category.id; categoriesOpen = false }) }
                    }
                }
                OutlinedButton(onClick = { dateOpen = true }, enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))) {
                    Text("Ngày: ${LocalDate.parse(date).format(dateFormat)}")
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú (không bắt buộc)") }, enabled = !busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), minLines = 2, maxLines = 4)
                (validation ?: actionError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp), enabled = !busy && categories.isNotEmpty(), onClick = {
                amountChecked = true
                validation = null
                val value = amount.trim().toLongOrNull()
                if (AmountValidator.errorForInput(amount) == null && value != null) {
                    if (categories.none { it.id == categoryId }) validation = "Vui lòng chọn danh mục"
                    else onSave(TransactionEntity(existing?.id ?: 0, type, value, categoryId, date, note))
                }
            }) { Text(if (busy) "Đang lưu…" else "Lưu") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Hủy") } }
    )
    if (dateOpen) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(onDismissRequest = { dateOpen = false },
            confirmButton = { TextButton(onClick = {
                picker.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                dateOpen = false
            }, enabled = picker.selectedDateMillis != null) { Text("Chọn") } },
            dismissButton = { TextButton(onClick = { dateOpen = false }) { Text("Hủy") } }
        ) { DatePicker(state = picker) }
    }
}
