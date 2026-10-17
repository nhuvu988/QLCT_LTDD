package com.example.qlct.view.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qlct.view.components.formatMoney
import com.example.qlct.view.components.BottomNavigation
import com.example.qlct.view.components.CategoryChart
import com.example.qlct.view.components.DashboardCard
import com.example.qlct.view.components.DashboardHeader
import com.example.qlct.view.components.ExpenseCalendar
import com.example.qlct.view.components.SpendingChart
import com.example.qlct.view.components.TransactionTile
import com.example.qlct.view.theme.Muted
import com.example.qlct.viewmodel.ExpenseViewModel


/**
 * Màn hình chi tiêu: quan sát uiState, hiển thị nội dung theo tab và gửi thao tác đến ViewModel.
 * Chỉ giữ trạng thái giao diện tạm như tab, ngày chọn và hộp thoại; dữ liệu lâu dài nằm trong Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(model: ExpenseViewModel, username: String = "", onLogout: () -> Unit = {}) {
    val state by model.uiState.collectAsStateWithLifecycle()
    val busy = state.busy
    val actionError = state.actionError
    val completedActions = state.completedActions
    // State của các hộp thoại được lưu bằng rememberSaveable để phục hồi khi Activity tái tạo.
    var budgetMonth by rememberSaveable { mutableStateOf<String?>(null) }
    var budgetAmount by rememberSaveable { mutableStateOf<Long?>(null) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var handledActions by rememberSaveable { mutableLongStateOf(0L) }
    // Chỉ đóng các hộp thoại khi có thao tác mới thành công; nếu lỗi thì giữ nguyên nội dung để thử lại.
    LaunchedEffect(completedActions) {
        if (completedActions > handledActions) {
            budgetMonth = null
            editorOpen = false
            deletingId = null
            handledActions = completedActions
        }
    }
    // Lưu ID đang sửa/xóa rồi tra bản ghi từ state mới nhất, tránh giữ một bản sao dữ liệu đã cũ.
    val editing = state.summary.visible.find { it.id == editingId }
    val deleting = state.summary.visible.find { it.id == deletingId }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    // Đổi tháng hoặc chế độ thời gian thì bỏ ngày đang chọn để không lọc nhầm ngày ngoài khoảng xem.
    LaunchedEffect(state.month, state.monthOnly) { selectedDate = null }
    // Tab Lịch áp dụng thêm tìm kiếm và ngày chọn; các tab khác dùng danh sách đã lọc tháng/loại.
    val displayRows = if (tab == 1) state.searchResults.filter { selectedDate == null || it.date == selectedDate } else state.summary.visible
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    // Mỗi lần chuyển tab đưa danh sách về đầu trang.
    LaunchedEffect(tab) { listState.scrollToItem(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                        Text(when (tab) { 1 -> "Lịch giao dịch"; 2 -> "Báo cáo"; 3 -> "Ngân sách"; else -> "Sổ thu chi" },
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(username, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    TextButton(onClick = onLogout, enabled = !busy,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                        Text("Đăng xuất", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigation(tab, {
                tab = it
                // Tab Ngân sách cố định phạm vi tháng và mọi loại để các số liệu theo danh mục thống nhất.
                if (it == 3) { model.setMonthOnly(true); model.setType("ALL") }
            }, {
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
                    DashboardHeader(state, { model.changeMonth(-1) }, { model.changeMonth(1) }, model::setMonthOnly, allowAllTime = tab != 3)
                }
                if (tab == 0 || tab == 3) {
                    item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            MonthlyBudgetCard(state.month, state.budget, busy) {
                                budgetMonth = state.month.toString()
                                budgetAmount = state.budget.limit
                                model.clearError()
                            }
                        }
                    }
                }
                // Tổng quan và Báo cáo dùng cùng dữ liệu biểu đồ; Báo cáo bật phần số tiền chi tiết.
                if (tab == 0 || tab == 2) {
                    item { Box(Modifier.padding(horizontal = 16.dp)) { CategoryChart(state.summary, detailed = tab == 2) } }
                    item { Box(Modifier.padding(horizontal = 16.dp)) { SpendingChart(state.summary.chart, detailed = tab == 2) } }
                }
                if (tab == 3) {
                    item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            DashboardCard("Chi tiêu trong tháng") {
                                state.categories.forEach { category ->
                                    val amount = state.summary.categoryExpenses.find { it.id == category.id }?.amount ?: 0L
                                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        com.example.qlct.view.components.Glyph(when (category.id) { 1L -> "food"; 2L -> "car"; 4L -> "bag"; 5L -> "home"; else -> "wallet" },
                                            com.example.qlct.view.components.categoryColor(category.id), Modifier.size(24.dp))
                                        Text(category.name, Modifier.weight(1f).padding(horizontal = 12.dp))
                                        Text(formatMoney(amount), style = MaterialTheme.typography.bodySmall)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
                if (tab == 1 && state.monthOnly) {
                    item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            ExpenseCalendar(state.month, state.searchResults, selectedDate) { selectedDate = it }
                        }
                    }
                }
                if (tab == 0 || tab == 1) {
                    item {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (tab == 0) "Giao dịch gần đây" else if (selectedDate == null) "Lịch sử giao dịch" else "Giao dịch ngày ${java.time.LocalDate.parse(selectedDate).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (tab == 0) TextButton(onClick = { tab = 1 }) { Text("Xem tất cả", fontSize = 11.sp) }
                            }
                            if (tab == 1) OutlinedTextField(
                                value = state.query, onValueChange = model::setQuery,
                                label = { Text("Tìm ghi chú hoặc danh mục") }, singleLine = true,
                                supportingText = { Text("Có thể gõ không dấu. Kết hợp với bộ lọc tháng và Thu/Chi.") },
                                trailingIcon = { if (state.query.isNotEmpty()) TextButton(onClick = { model.setQuery("") }) { Text("Xóa") } },
                                modifier = Modifier.fillMaxWidth())
                            if (tab == 1) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ALL" to "Tất cả", "INCOME" to "Thu", "EXPENSE" to "Chi").forEach { (value, label) ->
                                    FilterChip(selected = state.type == value, onClick = { model.setType(value) }, label = { Text(label) })
                                }
                            }
                            Text(if (tab == 0) "${displayRows.size} giao dịch · ${if (state.type == "INCOME") "Khoản thu" else if (state.type == "EXPENSE") "Khoản chi" else "Thu và chi"}" else "${displayRows.size} giao dịch · Chạm để sửa", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (displayRows.isEmpty()) item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            DashboardCard(if (tab == 1 && state.query.isNotBlank()) "Không tìm thấy giao dịch" else "Chưa có giao dịch") {
                                Text(if (tab == 1 && state.query.isNotBlank()) "Thử từ khóa khác hoặc thay đổi bộ lọc tháng, Thu/Chi." else "Chọn Nhập vào bên dưới để ghi lại khoản thu hoặc chi của bạn.", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    // Tổng quan chỉ lấy 4 giao dịch mới nhất; khóa ID giúp Compose nhận diện dòng khi danh sách đổi.
                    items(if (tab == 0) displayRows.take(4) else displayRows, key = { it.id }) { transaction ->
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

    // Các hộp thoại chuyển sự kiện về ViewModel; không tự ghi trực tiếp vào database.
    budgetMonth?.let { selectedMonth ->
        BudgetEditor(selectedMonth, budgetAmount, busy, actionError,
            onDismiss = { budgetMonth = null; model.clearError() },
            onSave = { model.saveBudget(selectedMonth, it) },
            onDelete = { model.deleteBudget(selectedMonth) })
    }
    if (editorOpen && !state.loading && state.loadError == null) {
        TransactionEditor(existing = editing, categories = state.categories, busy = busy, actionError = actionError,
            onDismiss = { editorOpen = false; model.clearError() },
            onSave = model::save)
    }
    // Xóa phải qua xác nhận; chỉ đóng sau khi completedActions tăng để lỗi xóa vẫn còn hiển thị.
    if (deleting != null) AlertDialog(
        onDismissRequest = { if (!busy) { deletingId = null; model.clearError() } },
        title = { Text("Xóa giao dịch?") },
        text = {
            Column {
                Text("${deleting.categoryName}: ${formatMoney(deleting.amount)}\nThao tác này không thể hoàn tác.")
                actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = { model.delete(deleting) }) { Text(if (busy) "Đang xóa…" else "Xóa") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { deletingId = null; model.clearError() }) { Text("Hủy") } }
    )
}
