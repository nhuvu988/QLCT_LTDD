package com.example.qlct.data

import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.flowOf

class ExpenseRepository(private val dao: ExpenseDao, private val userId: Int? = null) {
    val categories = dao.observeCategories()
    val transactions = dao.observeTransactions(userId)
    val budgets = userId?.let(dao::observeBudgets) ?: flowOf(emptyList<MonthlyBudget>())

    suspend fun saveBudget(month: String, amount: Long) {
        val owner = requireNotNull(userId) { "Vui lòng đăng nhập" }
        require(YearMonth.parse(month).toString() == month) { "Tháng không hợp lệ" }
        require(AmountValidator.errorForValue(amount) == null) { "Ngân sách không hợp lệ" }
        dao.saveBudget(MonthlyBudget(owner, month, amount))
    }

    suspend fun deleteBudget(month: String) {
        val owner = requireNotNull(userId) { "Vui lòng đăng nhập" }
        require(YearMonth.parse(month).toString() == month)
        dao.deleteBudget(owner, month)
    }


    suspend fun save(transaction: TransactionEntity) {
        require(transaction.type in listOf("INCOME", "EXPENSE")) { "Loại giao dịch không hợp lệ" }
        val amountError = AmountValidator.errorForValue(transaction.amount)
        require(amountError == null) { amountError ?: "Số tiền không hợp lệ" }
        require(transaction.categoryId > 0) { "Vui lòng chọn danh mục" }
        require(LocalDate.parse(transaction.date).toString() == transaction.date) { "Ngày không hợp lệ" }
        val normalized = transaction.copy(note = transaction.note.trim(), userId = userId)
        if (normalized.id == 0L) dao.insert(normalized)
        else check(dao.updateOwned(normalized.id, userId, normalized.type, normalized.amount,
            normalized.categoryId, normalized.date, normalized.note) == 1) { "Giao dịch không còn tồn tại" }
    }

    suspend fun delete(id: Long) {
        check(dao.deleteOwned(id, userId) == 1) { "Giao dịch không còn tồn tại" }
    }
}
