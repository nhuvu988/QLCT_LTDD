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

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun create(dao: FakeDao): ExpenseViewModel = ExpenseViewModel(
        ExpenseRepository(dao, 1),
        SavedStateHandle(mapOf("month" to "2026-10"))
    ).also { store.put("expense", it) }

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
        override fun observeCategories(): Flow<List<Category>> = flow {
            check(!failLoad) { "Simulated read error" }
            emit(listOf(Category(1, "Ăn uống")))
        }
        override fun observeTransactions(userId: Int?) = flowOf(listOf(
            TransactionRow(1, "EXPENSE", 45000, 1, "2026-10-12", "Ăn trưa", "Ăn uống")
        ))
        override fun observeBudgets(userId: Int) = budgets
        override suspend fun saveBudget(budget: MonthlyBudget) {
            saveCalls++
            delay(1000)
            check(!failSave) { "Simulated write error" }
            budgets.value = listOf(budget)
        }
        override suspend fun deleteBudget(userId: Int, month: String) { budgets.value = emptyList() }
        override suspend fun insertCategoriesIfMissing(categories: List<Category>) = Unit
        override suspend fun insert(transaction: TransactionEntity): Long = error("Not used")
        override suspend fun update(transaction: TransactionEntity): Int = error("Not used")
        override suspend fun updateOwned(id: Long, userId: Int?, type: String, amount: Long, categoryId: Long, date: String, note: String): Int = error("Not used")
        override suspend fun deleteOwned(id: Long, userId: Int?): Int = error("Not used")
        override suspend fun delete(id: Long): Int = error("Not used")
    }
}
