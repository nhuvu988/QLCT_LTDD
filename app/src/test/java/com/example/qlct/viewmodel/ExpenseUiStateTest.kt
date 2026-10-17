package com.example.qlct.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.qlct.model.dao.ExpenseDao
import com.example.qlct.model.entity.*
import com.example.qlct.model.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseUiStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    /**
     * Thay Main dispatcher bằng bộ điều phối kiểm thử để chủ động chạy các coroutine.
     */
    /**
     * Hủy ViewModel trong store và khôi phục Main dispatcher sau mỗi ca kiểm thử.
     */
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    /**
     * Tạo ExpenseViewModel với DAO giả, tài khoản 1 và tháng cố định để kết quả không phụ thuộc ngày chạy.
     */
    private fun create(dao: FakeDao): ExpenseViewModel = ExpenseViewModel(
        ExpenseRepository(dao, 1),
        SavedStateHandle(mapOf("month" to "2026-10"))
    ).also { store.put("expense", it) }

    /**
     * Kiểm tra uiState kết hợp dữ liệu, tìm kiếm, loại giao dịch và ngân sách khi các Flow thay đổi.
     */
    @Test fun stateCombinesDataFiltersAndBudget() = runTest {
        val dao = FakeDao()
        val vm = create(dao)
        assertTrue(vm.uiState.value.loading)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent()
        assertFalse(vm.uiState.value.loading)
        assertEquals(45000L, vm.uiState.value.summary.expense)
        vm.setQuery("an trua")
        vm.setType("EXPENSE")
        runCurrent()
        assertEquals(1, vm.uiState.value.searchResults.size)
        assertEquals("an trua", vm.uiState.value.query)
        dao.budgets.value = listOf(MonthlyBudget(1, "2026-10", 100000))
        runCurrent()
        assertEquals(100000L, vm.uiState.value.budget.limit)
        assertEquals(45000L, vm.uiState.value.budget.spent)
    }

    /**
     * Kiểm tra chặn gửi lặp, trạng thái busy, lỗi lưu và bộ đếm thành công sau khi thử lại.
     */
    @Test fun savePublishesBusyErrorAndCompletionWithoutDoubleSubmission() = runTest {
        val dao = FakeDao()
        val vm = create(dao)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent()
        dao.failSave = true
        vm.saveBudget("2026-10", 100000)
        vm.saveBudget("2026-10", 200000)
        runCurrent()
        assertTrue(vm.uiState.value.busy)
        assertEquals(1, dao.saveCalls)
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(vm.uiState.value.busy)
        assertNotNull(vm.uiState.value.actionError)
        assertEquals(0L, vm.uiState.value.completedActions)
        vm.clearError()
        runCurrent()
        assertNull(vm.uiState.value.actionError)
        dao.failSave = false
        vm.saveBudget("2026-10", 100000)
        runCurrent()
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(vm.uiState.value.busy)
        assertEquals(1L, vm.uiState.value.completedActions)
        assertEquals(100000L, vm.uiState.value.budget.limit)
    }

    /**
     * Mô phỏng lỗi đọc rồi thử tải lại; xác nhận lỗi được xóa và dữ liệu xuất hiện.
     */
    @Test fun retryRecoversFromLoadFailure() = runTest {
        val dao = FakeDao().apply { failLoad = true }
        val vm = create(dao)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent()
        assertFalse(vm.uiState.value.loading)
        assertNotNull(vm.uiState.value.loadError)
        dao.failLoad = false
        vm.retryLoad()
        runCurrent()
        assertNull(vm.uiState.value.loadError)
        assertFalse(vm.uiState.value.loading)
        assertEquals(1, vm.uiState.value.categories.size)
    }

    private class FakeDao : ExpenseDao {
        var failLoad = false
        var failSave = false
        var saveCalls = 0
        val budgets = MutableStateFlow(emptyList<MonthlyBudget>())
        /**
         * DAO giả phát danh mục cố định hoặc ném lỗi khi failLoad được bật.
         */
        override fun observeCategories(): Flow<List<Category>> = flow {
            check(!failLoad) { "Simulated read error" }
            emit(listOf(Category(1, "Ăn uống")))
        }
        /**
         * DAO giả trả một khoản chi ăn trưa cố định phục vụ kiểm tra UI state.
         */
        override fun observeTransactions(userId: Int?) = flowOf(listOf(
            TransactionRow(1, "EXPENSE", 45000, 1, "2026-10-12", "Ăn trưa", "Ăn uống")
        ))
        /**
         * DAO giả trả MutableStateFlow để bài kiểm thử chủ động thay đổi ngân sách.
         */
        override fun observeBudgets(userId: Int) = budgets
        /**
         * Mô phỏng ghi chậm một giây, đếm số lần gọi và có thể gây lỗi theo failSave.
         */
        override suspend fun saveBudget(budget: MonthlyBudget) {
            saveCalls++
            delay(1000)
            check(!failSave) { "Simulated write error" }
            budgets.value = listOf(budget)
        }
        /**
         * Mô phỏng xóa bằng cách phát danh sách ngân sách rỗng.
         */
        override suspend fun deleteBudget(userId: Int, month: String) { budgets.value = emptyList() }
        /**
         * Không làm gì trong DAO giả vì các ca kiểm thử này không chạy khởi tạo dữ liệu.
         */
        override suspend fun insertCategoriesIfMissing(categories: List<Category>) = Unit
        /**
         * Báo lỗi nếu bị gọi vì các ca kiểm thử này không dùng chức năng thêm giao dịch.
         */
        override suspend fun insert(transaction: TransactionEntity): Long = error("Not used")
        /**
         * Báo lỗi nếu bị gọi vì các ca kiểm thử này không dùng chức năng sửa giao dịch.
         */
        override suspend fun updateOwned(id: Long, userId: Int?, type: String, amount: Long, categoryId: Long, date: String, note: String): Int = error("Not used")
        /**
         * Báo lỗi nếu bị gọi vì các ca kiểm thử này không dùng chức năng xóa giao dịch.
         */
        override suspend fun deleteOwned(id: Long, userId: Int?): Int = error("Not used")
    }
}
