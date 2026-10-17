package com.example.qlct.viewmodel.state

import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.model.summary.BudgetStatus
import com.example.qlct.model.summary.ExpenseSummary
import java.time.YearMonth

// Nguồn trạng thái duy nhất của màn hình chi tiêu: dữ liệu, bộ lọc và phản hồi thao tác.
data class ExpenseUiState(
    val loading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val month: YearMonth = YearMonth.now(),
    val monthOnly: Boolean = true,
    val type: String = "ALL",
    val summary: ExpenseSummary = ExpenseSummary(0, 0, emptyList(), emptyList()),
    // Lỗi đọc dữ liệu dùng màn hình thử lại; actionError bên dưới là lỗi ghi, giữ nguyên biểu mẫu.
    val loadError: String? = null,
    val query: String = "",
    // Kết quả tìm kiếm từ summary.visible; không thay thế các số tổng hợp dùng cho header/biểu đồ.
    val searchResults: List<TransactionRow> = emptyList(),
    val budget: BudgetStatus = BudgetStatus(),
    val busy: Boolean = false,
    val actionError: String? = null,
    // Bộ đếm thao tác thành công giúp UI nhận biết lần lưu/xóa mới để đóng hộp thoại tương ứng.
    val completedActions: Long = 0
)
