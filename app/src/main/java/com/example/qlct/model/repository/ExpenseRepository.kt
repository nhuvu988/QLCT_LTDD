package com.example.qlct.model.repository

import com.example.qlct.model.dao.ExpenseDao
import com.example.qlct.model.entity.MonthlyBudget
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.validation.AmountValidator
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.flowOf

// Lớp trung gian giữa ViewModel và DAO, chịu trách nhiệm validation và giới hạn dữ liệu theo userId.
class ExpenseRepository(private val dao: ExpenseDao, private val userId: Int? = null) {
    // Các Flow Room tự phát dữ liệu mới sau khi bảng thay đổi, nên UI không cần gọi tải lại sau mỗi lần lưu.
    val categories = dao.observeCategories()
    val transactions = dao.observeTransactions(userId)
    // Chưa có tài khoản thì phát danh sách ngân sách rỗng; danh mục vẫn là dữ liệu dùng chung.
    val budgets = userId?.let(dao::observeBudgets) ?: flowOf(emptyList<MonthlyBudget>())

    /**
     * Yêu cầu đã đăng nhập, tháng đúng dạng yyyy-MM và hạn mức hợp lệ trước khi Upsert ngân sách.
     */
    suspend fun saveBudget(month: String, amount: Long) {
        val owner = requireNotNull(userId) { "Vui lòng đăng nhập" }
        require(YearMonth.parse(month).toString() == month) { "Tháng không hợp lệ" }
        require(AmountValidator.errorForValue(amount) == null) { "Ngân sách không hợp lệ" }
        dao.saveBudget(MonthlyBudget(owner, month, amount))
    }

    /**
     * Kiểm tra tài khoản và định dạng tháng rồi xóa đúng hạn mức của tài khoản hiện tại.
     */
    suspend fun deleteBudget(month: String) {
        val owner = requireNotNull(userId) { "Vui lòng đăng nhập" }
        require(YearMonth.parse(month).toString() == month)
        dao.deleteBudget(owner, month)
    }


    /**
     * Kiểm tra loại, số tiền, danh mục và ngày trước khi ghi giao dịch.
     * ID bằng 0 là thêm mới; ID khác 0 là sửa và phải khớp chủ sở hữu của Repository.
     */
    suspend fun save(transaction: TransactionEntity) {
        require(transaction.type in listOf("INCOME", "EXPENSE")) { "Loại giao dịch không hợp lệ" }
        val amountError = AmountValidator.errorForValue(transaction.amount)
        require(amountError == null) { amountError ?: "Số tiền không hợp lệ" }
        require(transaction.categoryId > 0) { "Vui lòng chọn danh mục" }
        require(LocalDate.parse(transaction.date).toString() == transaction.date) { "Ngày không hợp lệ" }
        // Luôn lấy chủ sở hữu từ Repository thay vì tin userId truyền từ biểu mẫu; bỏ khoảng trắng thừa trong ghi chú.
        val normalized = transaction.copy(note = transaction.note.trim(), userId = userId)
        if (normalized.id == 0L) dao.insert(normalized)
        // DAO trả số dòng tác động; khác 1 nghĩa là bản ghi đã mất hoặc không thuộc tài khoản hiện tại.
        else check(dao.updateOwned(normalized.id, userId, normalized.type, normalized.amount,
            normalized.categoryId, normalized.date, normalized.note) == 1) { "Giao dịch không còn tồn tại" }
    }

    /**
     * Xóa giao dịch của tài khoản hiện tại; báo lỗi nếu không có đúng một dòng bị xóa.
     */
    suspend fun delete(id: Long) {
        check(dao.deleteOwned(id, userId) == 1) { "Giao dịch không còn tồn tại" }
    }
}
