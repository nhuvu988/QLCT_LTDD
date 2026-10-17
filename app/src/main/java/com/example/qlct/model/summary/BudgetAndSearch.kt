package com.example.qlct.model.summary

import com.example.qlct.model.entity.TransactionRow
import java.text.Normalizer
import java.util.Locale

// Các mức: chưa đặt, bình thường, từ 80%, đúng 100% và vượt 100% hạn mức.
enum class BudgetLevel { NONE, NORMAL, WARNING, REACHED, EXCEEDED }

// Trạng thái ngân sách tính từ hạn mức và tổng chi tháng; không lưu thành bảng riêng.
data class BudgetStatus(val limit: Long? = null, val spent: Long = 0) {
    // null khi chưa đặt hạn mức; giá trị âm biểu thị số tiền vượt ngân sách.
    val remaining: Long? get() = limit?.minus(spent)
    // Tỷ lệ thực có thể lớn hơn 1; progress chỉ dùng vẽ thanh tiến trình nên chặn trong 0..1.
    val ratio: Float get() = if (limit != null && limit > 0) spent.toFloat() / limit else 0f
    val progress: Float get() = ratio.coerceIn(0f, 1f)
    val level: BudgetLevel get() = when {
        limit == null || limit <= 0 -> BudgetLevel.NONE
        // Kiểm tra vượt/hết mức trước cảnh báo 80% để chọn đúng mức nghiêm trọng nhất.
        spent > limit -> BudgetLevel.EXCEEDED
        spent == limit -> BudgetLevel.REACHED
        spent.toDouble() / limit >= 0.8 -> BudgetLevel.WARNING
        else -> BudgetLevel.NORMAL
    }
}

/**
 * Chuyển thành chữ thường và bỏ dấu tiếng Việt, gồm cả đ, để hỗ trợ tìm kiếm không dấu.
 */
private fun normalizeSearch(value: String): String =
    Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace('đ', 'd')

/**
 * Lọc danh sách đã chọn theo từng từ khóa trong ghi chú hoặc tên danh mục.
 * Mọi từ đều phải xuất hiện; từ khóa rỗng trả nguyên danh sách, không tính lại tổng thu/chi.
 */
fun searchTransactions(rows: List<TransactionRow>, query: String): List<TransactionRow> {
    val words = normalizeSearch(query.trim()).split(Regex("\\s+")).filter(String::isNotEmpty)
    if (words.isEmpty()) return rows
    // Từng từ được tìm trên chuỗi ghép ghi chú và danh mục; các từ có thể khớp ở hai phần khác nhau.
    return rows.filter { row ->
        val text = normalizeSearch("${row.note} ${row.categoryName}")
        words.all(text::contains)
    }
}
