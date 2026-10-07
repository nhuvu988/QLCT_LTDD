package com.example.qlct.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qlct.data.Category
import com.example.qlct.data.ExpenseRepository
import com.example.qlct.data.TransactionEntity
import com.example.qlct.data.TransactionRow
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val loading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val month: YearMonth = YearMonth.now(),
    val monthOnly: Boolean = true,
    val type: String = "ALL",
    val summary: ExpenseSummary = ExpenseSummary(0, 0, emptyList(), emptyList()),
    val loadError: String? = null,
    val query: String = "",
    val searchResults: List<TransactionRow> = emptyList(),
    val budget: BudgetStatus = BudgetStatus()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val repository: ExpenseRepository, private val savedState: SavedStateHandle) : ViewModel() {
    private val month = savedState.getStateFlow("month", YearMonth.now().toString())
    private val monthOnly = savedState.getStateFlow("monthOnly", true)
    private val type = savedState.getStateFlow("type", "ALL")
    private val query = savedState.getStateFlow("query", "")
    private val retry = MutableStateFlow(0)
    val busy = MutableStateFlow(false)
    val actionError = MutableStateFlow<String?>(null)
    val completedActions = MutableStateFlow(0L)

    val state = retry.flatMapLatest {
        val base = combine(repository.categories, repository.transactions, month, monthOnly, type) { categories, rows, selectedMonth, only, filter ->
            val selected = YearMonth.parse(selectedMonth)
            ExpenseUiState(false, categories, selected, only, filter, summarize(rows, selected, only, filter))
        }
        combine(base, repository.budgets, query) { current, budgets, text ->
            current.copy(query = text,
                searchResults = searchTransactions(current.summary.visible, text),
                budget = BudgetStatus(budgets.find { it.month == current.month.toString() }?.amount,
                    current.summary.chart.lastOrNull()?.amount ?: 0))
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(ExpenseUiState(loading = false, loadError = "Không tải được dữ liệu. Vui lòng thử lại."))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    fun changeMonth(delta: Long) { savedState["month"] = YearMonth.parse(month.value).plusMonths(delta).toString() }
    fun setMonthOnly(value: Boolean) { savedState["monthOnly"] = value }
    fun setType(value: String) { savedState["type"] = value }
    fun setQuery(value: String) { savedState["query"] = value }
    fun saveBudget(month: String, amount: Long) = mutate { repository.saveBudget(month, amount) }
    fun deleteBudget(month: String) = mutate { repository.deleteBudget(month) }
    fun retryLoad() { retry.value++ }
    fun clearError() { actionError.value = null }

    fun save(transaction: TransactionEntity) = mutate { repository.save(transaction) }
    fun delete(transaction: TransactionRow) = mutate { repository.delete(transaction.id) }

    private fun mutate(action: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        actionError.value = null
        viewModelScope.launch {
            try {
                action()
                completedActions.value++
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                actionError.value = "Không thực hiện được. Dữ liệu nhập được giữ để thử lại."
            } finally {
                busy.value = false
            }
        }
    }

    companion object {
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseViewModel(repository, createSavedStateHandle()) }
        }
    }
}
