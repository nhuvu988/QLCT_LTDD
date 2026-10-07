package com.example.qlct.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qlct.data.Category
import com.example.qlct.data.AmountValidator
import com.example.qlct.data.TransactionEntity
import com.example.qlct.data.TransactionRow
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun money(amount: Long): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " đ"
private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(model: ExpenseViewModel, username: String = "", onLogout: () -> Unit = {}) {
    val state by model.state.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    val actionError by model.actionError.collectAsStateWithLifecycle()
    val completedActions by model.completedActions.collectAsStateWithLifecycle()
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var handledActions by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(completedActions) {
        if (completedActions > handledActions) {
            editorOpen = false
            deletingId = null
            handledActions = completedActions
        }
    }
    val editing = state.summary.visible.find { it.id == editingId }
    val deleting = state.summary.visible.find { it.id == deletingId }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(tab) { listState.scrollToItem(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = Purple, contentColor = androidx.compose.ui.graphics.Color.White) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Tài khoản: $username", modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    TextButton(onClick = onLogout, enabled = !busy,
                        colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color.White)) {
                        Text("Đăng xuất", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigation(tab, { tab = it }, {
                editingId = null; model.clearError(); editorOpen = true
            }, !state.loading && state.loadError == null && state.categories.isNotEmpty() && !busy)
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.loadError != null -> Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(state.loadError!!)
                Button(onClick = model::retryLoad) { Text("Thử lại") }
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DashboardHeader(state, { model.changeMonth(-1) }, { model.changeMonth(1) }, model::setMonthOnly)
                }
                if (tab != 1) {
                    item { Box(Modifier.padding(horizontal = 16.dp)) { CategoryChart(state.summary, detailed = tab == 2) } }
                    item { Box(Modifier.padding(horizontal = 16.dp)) { SpendingChart(state.summary.chart, detailed = tab == 2) } }
                }
                if (tab != 2) {
                    item {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (tab == 0) "Giao dịch gần đây" else "Lịch sử giao dịch", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (tab == 0) TextButton(onClick = { tab = 1 }) { Text("Xem tất cả", fontSize = 11.sp) }
                            }
                            if (tab == 1) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ALL" to "Tất cả", "INCOME" to "Thu", "EXPENSE" to "Chi").forEach { (value, label) ->
                                    FilterChip(selected = state.type == value, onClick = { model.setType(value) }, label = { Text(label) })
                                }
                            }
                            Text(if (tab == 0) "${state.summary.visible.size} giao dịch · ${if (state.type == "INCOME") "Khoản thu" else if (state.type == "EXPENSE") "Khoản chi" else "Thu và chi"}" else "${state.summary.visible.size} giao dịch · Chạm để sửa", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (state.summary.visible.isEmpty()) item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            DashboardCard("Chưa có giao dịch") {
                                Text("Nhấn nút + bên dưới để ghi lại khoản thu hoặc chi của bạn.", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    items(if (tab == 0) state.summary.visible.take(4) else state.summary.visible, key = { it.id }) { transaction ->
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            TransactionTile(transaction, busy, {
                                editingId = transaction.id; model.clearError(); editorOpen = true
                            }, { model.clearError(); deletingId = transaction.id })
                        }
                    }
                }
            }
        }
    }

    if (editorOpen && !state.loading && state.loadError == null) {
        TransactionEditor(existing = editing, categories = state.categories, busy = busy, actionError = actionError,
            onDismiss = { editorOpen = false; model.clearError() },
            onSave = model::save)
    }
    if (deleting != null) AlertDialog(
        onDismissRequest = { if (!busy) { deletingId = null; model.clearError() } },
        title = { Text("Xóa giao dịch?") },
        text = {
            Column {
                Text("${deleting.categoryName}: ${money(deleting.amount)}\nThao tác này không thể hoàn tác.")
                actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = { model.delete(deleting) }) { Text(if (busy) "Đang xóa…" else "Xóa") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { deletingId = null; model.clearError() }) { Text("Hủy") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionEditor(
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
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(if (existing == null) "Thêm giao dịch" else "Sửa giao dịch") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == "EXPENSE", onClick = { type = "EXPENSE" }, enabled = !busy, label = { Text("Chi tiền") })
                    FilterChip(type == "INCOME", onClick = { type = "INCOME" }, enabled = !busy, label = { Text("Thu tiền") })
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it; validation = null }, label = { Text("Số tiền (VND)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !busy,
                    isError = amountError != null,
                    supportingText = { Text(amountError ?: "Nhập số tiền lớn hơn 0; chọn Thu hoặc Chi ở trên") },
                    modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { categoriesOpen = true }, enabled = !busy) {
                        Text("Danh mục: ${categories.find { it.id == categoryId }?.name ?: "Chọn danh mục"}")
                    }
                    DropdownMenu(expanded = categoriesOpen, onDismissRequest = { categoriesOpen = false }) {
                        categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { categoryId = category.id; categoriesOpen = false }) }
                    }
                }
                OutlinedButton(onClick = { dateOpen = true }, enabled = !busy) {
                    Text("Ngày: ${LocalDate.parse(date).format(dateFormat)}")
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú (không bắt buộc)") }, enabled = !busy, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                (validation ?: actionError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && categories.isNotEmpty(), onClick = {
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
