package com.example.qlct.view.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.qlct.view.components.Glyph
import com.example.qlct.view.components.categoryColor
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
    var dateOpen by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, enabled = !busy) { Text("Đóng") }
                    Text(if (existing == null) "Ghi chép thu chi" else "Sửa giao dịch", Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(64.dp))
                }
                HorizontalDivider()
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EXPENSE" to "Tiền chi", "INCOME" to "Tiền thu").forEach { (value, label) ->
                            val selected = type == value
                            Button(onClick = { type = value }, enabled = !busy, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { date = LocalDate.parse(date).minusDays(1).toString() }, enabled = !busy,
                            modifier = Modifier.semantics { contentDescription = "Ngày trước" }) { Text("‹", fontSize = 28.sp) }
                        FilledTonalButton(onClick = { dateOpen = true }, enabled = !busy, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                            Text(LocalDate.parse(date).format(dateFormat), fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { date = LocalDate.parse(date).plusDays(1).toString() }, enabled = !busy,
                            modifier = Modifier.semantics { contentDescription = "Ngày sau" }) { Text("›", fontSize = 28.sp) }
                    }
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") },
                        placeholder = { Text("Bạn đã chi / thu cho việc gì?") }, enabled = !busy,
                        modifier = Modifier.fillMaxWidth(), maxLines = 4, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it; validation = null },
                        label = { Text(if (type == "EXPENSE") "Số tiền chi" else "Số tiền thu") },
                        suffix = { Text("₫") }, textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !busy,
                        isError = amountError != null, supportingText = { amountError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Text("Danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    categories.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { category ->
                                val selected = categoryId == category.id
                                val color = categoryColor(category.id)
                                Surface(onClick = { categoryId = category.id }, enabled = !busy,
                                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(if (selected) 2.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                                    Column(Modifier.padding(horizontal = 4.dp, vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Glyph(when (category.id) { 1L -> "food"; 2L -> "car"; 3L -> "calendar"; 4L -> "bag"; 5L -> "home"; else -> "wallet" }, color, Modifier.size(28.dp))
                                        Text(category.name, fontSize = 12.sp, maxLines = 2, minLines = 2,
                                            overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    (validation ?: actionError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                Button(enabled = !busy && categories.isNotEmpty(), onClick = {
                    amountChecked = true
                    validation = null
                    val value = amount.trim().toLongOrNull()
                    if (AmountValidator.errorForInput(amount) == null && value != null) {
                        if (categories.none { it.id == categoryId }) validation = "Vui lòng chọn danh mục"
                        else onSave(TransactionEntity(existing?.id ?: 0, type, value, categoryId, date, note))
                    }
                }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(28.dp)) {
                    Text(if (busy) "Đang lưu…" else if (existing != null) "Lưu thay đổi" else if (type == "EXPENSE") "Nhập khoản chi" else "Nhập khoản thu",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
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
