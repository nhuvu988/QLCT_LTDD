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
import com.example.qlct.view.components.SpendingChart
import com.example.qlct.view.components.TransactionTile
import com.example.qlct.view.theme.Muted
import com.example.qlct.view.theme.Purple
import com.example.qlct.viewmodel.ExpenseViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(model: ExpenseViewModel, username: String = "", onLogout: () -> Unit = {}) {
    val state by model.uiState.collectAsStateWithLifecycle()
    val busy = state.busy
    val actionError = state.actionError
    val completedActions = state.completedActions
    var budgetMonth by rememberSaveable { mutableStateOf<String?>(null) }
    var budgetAmount by rememberSaveable { mutableStateOf<Long?>(null) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var handledActions by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(completedActions) {
        if (completedActions > handledActions) {
            budgetMonth = null
            editorOpen = false
            deletingId = null
            handledActions = completedActions
        }
    }
    val editing = state.summary.visible.find { it.id == editingId }
    val deleting = state.summary.visible.find { it.id == deletingId }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    val displayRows = if (tab == 1) state.searchResults else state.summary.visible
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
                    item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            MonthlyBudgetCard(state.month, state.budget, busy) {
                                budgetMonth = state.month.toString()
                                budgetAmount = state.budget.limit
                                model.clearError()
                            }
                        }
                    }
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
                                Text(if (tab == 1 && state.query.isNotBlank()) "Thử từ khóa khác hoặc thay đổi bộ lọc tháng, Thu/Chi." else "Nhấn nút + bên dưới để ghi lại khoản thu hoặc chi của bạn.", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
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
