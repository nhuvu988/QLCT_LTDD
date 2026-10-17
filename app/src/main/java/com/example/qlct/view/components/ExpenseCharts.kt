package com.example.qlct.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.model.summary.ExpenseSummary
import com.example.qlct.model.summary.MonthlyExpense
import com.example.qlct.view.theme.Muted
import com.example.qlct.view.theme.Purple
import java.util.Locale

private val categoryColors = listOf(Purple, Color(0xFF66A0D7), Color(0xFFE7A25A), Color(0xFFA7BE71), Color(0xFF6AB3B2), Color(0xFFD9829C), Color(0xFF9B88D2))
/**
 * Ánh xạ ID danh mục sang bảng màu dùng chung bằng phép chia dư để màu nhất quán giữa các màn hình.
 */
internal fun categoryColor(id: Long) = categoryColors[((id - 1).coerceAtLeast(0) % categoryColors.size).toInt()]

/**
 * Vẽ biểu đồ vòng theo tỷ trọng khoản chi và chú giải từng danh mục.
 * detailed bật bảng số tiền chi tiết; khi tổng chi bằng 0, hiển thị vòng nền rỗng.
 */
@Composable
internal fun CategoryChart(summary: ExpenseSummary, detailed: Boolean = false) {
    DashboardCard("Chi tiêu theo danh mục", if (summary.expense == 0L) "Chưa có chi tiêu" else "${summary.categoryExpenses.size} danh mục") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(122.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().semantics {
                    contentDescription = summary.categoryExpenses.joinToString("; ") { "${it.name}: ${formatMoney(it.amount)}" }.ifEmpty { "Chưa có khoản chi" }
                }) {
                    val stroke = 25.dp.toPx()
                    // Lùi vào nửa độ dày nét để vòng tròn không bị cắt ở biên Canvas.
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    if (summary.expense == 0L) drawArc(Color(0xFFEDEAF5), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                    else {
                        // Góc -90 độ bắt đầu từ đỉnh; mỗi cung có góc bằng tỷ lệ tiền của danh mục nhân 360 độ.
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

/**
 * Vẽ biểu đồ cột từ tổng chi từng tháng; cột cuối được tô đậm là tháng đang chọn.
 * detailed bật danh sách số tiền bên dưới biểu đồ.
 */
@Composable
internal fun SpendingChart(points: List<MonthlyExpense>, detailed: Boolean = false) {
    DashboardCard("Chi tiêu theo tháng", "6 tháng · VND") {
        // Mẫu số tối thiểu bằng 1 để không chia cho 0 khi mọi tháng đều chưa có khoản chi.
        val max = points.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
        val grid = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.fillMaxWidth().height(90.dp).semantics {
            contentDescription = points.joinToString("; ") { "Tháng ${it.month.monthValue}/${it.month.year}: ${formatMoney(it.amount)}" }
        }) {
            if (points.isEmpty()) return@Canvas
            // Chia đều chiều rộng Canvas theo số tháng; mỗi cột chiếm một phần trong ô tháng.
            val slot = size.width / points.size
            repeat(3) { i ->
                val y = size.height * i / 2
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            points.forEachIndexed { index, item ->
                // Đổi tỷ lệ số tiền/max sang chiều cao pixel; hệ tọa độ Canvas có gốc ở góc trên trái.
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
