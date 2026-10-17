package com.example.qlct.model.summary

import com.example.qlct.model.entity.TransactionRow
import java.time.YearMonth

// Một điểm dữ liệu biểu đồ cột: tháng và tổng khoản chi trong tháng đó.
data class MonthlyExpense(val month: YearMonth, val amount: Long)
// Một phần biểu đồ vòng: ID/tên danh mục và tổng tiền chi.
data class CategoryExpense(val id: Long, val name: String, val amount: Long)
// Kết quả tổng hợp dùng chung cho header, danh sách giao dịch và hai biểu đồ.
data class ExpenseSummary(
    val income: Long,
    val expense: Long,
    val visible: List<TransactionRow>,
    val chart: List<MonthlyExpense>,
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

/**
 * Tính tổng thu/chi theo khoảng thời gian, danh sách theo loại và phân bổ chi theo danh mục.
 * Biểu đồ luôn lấy 6 tháng kết thúc ở month, chỉ cộng EXPENSE và không chịu bộ lọc loại.
 */
fun summarize(rows: List<TransactionRow>, month: YearMonth, monthOnly: Boolean, type: String): ExpenseSummary {
    // Lọc thời gian trước: monthOnly = false giữ toàn bộ; true chỉ giữ đúng tháng được chọn.
    val period = rows.filter { !monthOnly || it.date.startsWith(month.toString() + "-") }
    return ExpenseSummary(
        income = period.filter { it.type == "INCOME" }.sumOf { it.amount },
        expense = period.filter { it.type == "EXPENSE" }.sumOf { it.amount },
        // Bộ lọc Thu/Chi chỉ đổi danh sách, không làm thay đổi tổng thu/chi phía trên.
        visible = period.filter { type == "ALL" || it.type == type },
        // Luôn tạo đủ 6 điểm kể cả tháng không có khoản chi; tháng chọn nằm cuối danh sách.
        chart = (5L downTo 0L).map { offset ->
            val item = month.minusMonths(offset)
            MonthlyExpense(item, rows.filter { it.type == "EXPENSE" && it.date.startsWith(item.toString() + "-") }.sumOf { it.amount })
        },
        // Chỉ gom EXPENSE trong khoảng thời gian đã chọn, rồi xếp danh mục chi nhiều nhất lên trước.
        categoryExpenses = period.filter { it.type == "EXPENSE" }.groupBy { it.categoryId }.map { (id, transactions) ->
            CategoryExpense(id, transactions.first().categoryName, transactions.sumOf { it.amount })
        }.sortedByDescending { it.amount }
    )
}
