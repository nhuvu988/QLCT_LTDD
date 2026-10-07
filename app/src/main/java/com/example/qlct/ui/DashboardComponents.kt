package com.example.qlct.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.data.TransactionRow
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val Purple = Color(0xFF7962C8)
internal val Muted = Color(0xFF9995AA)
internal val IncomeGreen = Color(0xFF58A58E)
private val categoryColors = listOf(Purple, Color(0xFF66A0D7), Color(0xFFE7A25A), Color(0xFFA7BE71), Color(0xFF6AB3B2), Color(0xFFD9829C), Color(0xFF9B88D2))
internal fun formatMoney(amount: Long) = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " đ"
private fun categoryColor(id: Long) = categoryColors[((id - 1).coerceAtLeast(0) % categoryColors.size).toInt()]

@Composable
internal fun DashboardHeader(state: ExpenseUiState, previous: () -> Unit, next: () -> Unit, monthOnly: (Boolean) -> Unit) {
    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF57429F), Color(0xFF9684DC))))) {
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Color.White.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("Q", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("SỔ THU CHI CÁ NHÂN", color = Color.White.copy(alpha = .7f), fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp)
                    Text("Quản lý chi tiêu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
                }
                Glyph("wallet", Color.White, Modifier.size(24.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.White.copy(alpha = .15f), shape = RoundedCornerShape(50)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = previous, modifier = Modifier.size(40.dp)) { Text("‹", fontSize = 24.sp, color = Color.White, modifier = Modifier.semantics { contentDescription = "Tháng trước" }) }
                        Text("Tháng ${state.month.monthValue}, ${state.month.year}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        IconButton(onClick = next, modifier = Modifier.size(40.dp)) { Text("›", fontSize = 24.sp, color = Color.White, modifier = Modifier.semantics { contentDescription = "Tháng sau" }) }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { monthOnly(!state.monthOnly) }, contentPadding = PaddingValues(8.dp)) {
                    Text(if (state.monthOnly) "Theo tháng" else "Toàn bộ", color = Color.White, fontSize = 12.sp)
                }
            }
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Chênh lệch thu − chi", color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                            Text(formatMoney(state.summary.income - state.summary.expense), fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 32.sp, maxLines = 1)
                        }
                        Canvas(Modifier.size(42.dp, 32.dp)) {
                            repeat(4) { i ->
                                val h = size.height * (.25f + i * .25f)
                                drawRoundRect(Purple.copy(alpha = .4f + i * .2f), Offset(i * size.width / 4, size.height - h), Size(size.width / 7, h), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(Modifier.fillMaxWidth()) {
                        BalanceAmount("Tổng thu", state.summary.income, IncomeGreen, Modifier.weight(1f))
                        BalanceAmount("Tổng chi", state.summary.expense, Color(0xFFDB8296), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceAmount(label: String, amount: Long, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Text(label, color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Text(formatMoney(amount), fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
internal fun DashboardCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                if (subtitle != null) Text(subtitle, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            }
            content()
        }
    }
}

@Composable
internal fun CategoryChart(summary: ExpenseSummary, detailed: Boolean = false) {
    DashboardCard("Chi tiêu theo danh mục", if (summary.expense == 0L) "Chưa có chi tiêu" else "${summary.categoryExpenses.size} danh mục") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(122.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().semantics {
                    contentDescription = summary.categoryExpenses.joinToString("; ") { "${it.name}: ${formatMoney(it.amount)}" }.ifEmpty { "Chưa có khoản chi" }
                }) {
                    val stroke = 25.dp.toPx()
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    if (summary.expense == 0L) drawArc(Color(0xFFEDEAF5), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                    else {
                        var start = -90f
                        summary.categoryExpenses.forEach { item ->
                            val sweep = (item.amount.toDouble() / summary.expense * 360).toFloat()
                            drawArc(categoryColor(item.id), start, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                            start += sweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng chi", color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                    Text(if (summary.expense >= 1_000_000) "${String.format(Locale.forLanguageTag("vi-VN"), "%.1f", summary.expense / 1_000_000.0)} tr"
                        else if (summary.expense >= 1000) "${summary.expense / 1000} k" else summary.expense.toString(), fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (summary.categoryExpenses.isEmpty()) Text("Thêm khoản chi để xem phân bổ.", color = Muted, fontSize = 12.sp)
                summary.categoryExpenses.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(7.dp).background(categoryColor(item.id), CircleShape))
                        Text(item.name, Modifier.weight(1f), fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${(item.amount.toDouble() / summary.expense * 100).toInt()}%", color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
        }
        if (detailed) summary.categoryExpenses.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.name, fontSize = 12.sp)
                Text(formatMoney(item.amount), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
internal fun SpendingChart(points: List<MonthlyExpense>, detailed: Boolean = false) {
    DashboardCard("Chi tiêu theo tháng", "6 tháng · VND") {
        val max = points.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
        val grid = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.fillMaxWidth().height(90.dp).semantics {
            contentDescription = points.joinToString("; ") { "Tháng ${it.month.monthValue}/${it.month.year}: ${formatMoney(it.amount)}" }
        }) {
            if (points.isEmpty()) return@Canvas
            val slot = size.width / points.size
            repeat(3) { i ->
                val y = size.height * i / 2
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            points.forEachIndexed { index, item ->
                val height = (item.amount.toDouble() / max * (size.height - 2.dp.toPx())).toFloat()
                if (height > 0) drawRoundRect(if (index == points.lastIndex) Purple else Color(0xFFC6BCE8),
                    Offset(slot * index + slot * .33f, size.height - height), Size(slot * .34f, height), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            points.forEach { item -> Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(4.dp).background(if (item.amount > 0) Purple else Color(0xFFD8D5E3), CircleShape))
                Text("${item.month.monthValue}/${item.month.year % 100}", color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            } }
        }
        if (points.all { it.amount == 0L }) Text("Chưa có khoản chi trong giai đoạn này.", color = Muted, fontSize = 12.sp)
        if (detailed) points.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tháng ${item.month.monthValue}/${item.month.year}", fontSize = 12.sp)
                Text(formatMoney(item.amount), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

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

@Composable
internal fun BottomNavigation(selected: Int, onSelect: (Int) -> Unit, onAdd: () -> Unit, canAdd: Boolean) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 10.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
            NavigationButton("home", "Tổng quan", selected == 0) { onSelect(0) }
            NavigationButton("wallet", "Giao dịch", selected == 1) { onSelect(1) }
            FilledIconButton(onClick = onAdd, enabled = canAdd, modifier = Modifier.size(46.dp), shape = RoundedCornerShape(16.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Purple)) {
                Text("+", color = Color.White, fontSize = 28.sp, modifier = Modifier.semantics { contentDescription = "Thêm giao dịch" })
            }
            NavigationButton("chart", "Thống kê", selected == 2) { onSelect(2) }
        }
    }
}

@Composable
private fun NavigationButton(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Glyph(icon, if (selected) Purple else Muted, Modifier.size(20.dp))
        Text(label, fontSize = 10.sp, lineHeight = 14.sp, color = if (selected) Purple else Muted, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// Code-native line icons keep the same visual weight without adding image assets.
@Composable
internal fun Glyph(kind: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height; val stroke = 1.7.dp.toPx()
        fun line(x: Float, y: Float, xx: Float, yy: Float) = drawLine(color, Offset(w*x, h*y), Offset(w*xx, h*yy), stroke, StrokeCap.Round)
        when (kind) {
            "chart" -> { line(.2f,.85f,.2f,.55f); line(.5f,.85f,.5f,.32f); line(.8f,.85f,.8f,.1f) }
            "home" -> { line(.1f,.45f,.5f,.1f); line(.5f,.1f,.9f,.45f); line(.2f,.4f,.2f,.88f); line(.2f,.88f,.8f,.88f); line(.8f,.88f,.8f,.4f); line(.5f,.88f,.5f,.6f) }
            "food" -> { line(.25f,.1f,.25f,.9f); line(.1f,.1f,.1f,.35f); line(.4f,.1f,.4f,.35f); line(.1f,.35f,.4f,.35f); drawOval(color, Offset(w*.65f,h*.1f), Size(w*.25f,h*.38f), style = Stroke(stroke)); line(.78f,.48f,.78f,.9f) }
            "trash" -> { line(.15f,.25f,.85f,.25f); line(.35f,.12f,.65f,.12f); line(.25f,.25f,.3f,.9f); line(.3f,.9f,.7f,.9f); line(.7f,.9f,.75f,.25f); line(.45f,.42f,.45f,.72f); line(.6f,.42f,.6f,.72f) }
            "car" -> { line(.15f,.4f,.3f,.15f); line(.3f,.15f,.7f,.15f); line(.7f,.15f,.85f,.4f); drawRoundRect(color, Offset(w*.1f,h*.4f), Size(w*.8f,h*.35f), androidx.compose.ui.geometry.CornerRadius(w*.08f), style = Stroke(stroke)); line(.22f,.75f,.22f,.9f); line(.78f,.75f,.78f,.9f) }
            else -> { drawRoundRect(color, Offset(w*.12f,h*.25f), Size(w*.76f,h*.6f), androidx.compose.ui.geometry.CornerRadius(w*.1f), style = Stroke(stroke)); if (kind == "bag") { drawArc(color, 180f, 180f, false, Offset(w*.32f,h*.03f), Size(w*.36f,h*.42f), style = Stroke(stroke)) } else { line(.6f,.5f,.88f,.5f); drawCircle(color, w*.035f, Offset(w*.68f,h*.6f)) } }
        }
    }
}
