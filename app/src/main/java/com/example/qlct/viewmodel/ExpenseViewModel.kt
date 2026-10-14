package com.example.qlct.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.model.repository.ExpenseRepository
import com.example.qlct.model.summary.BudgetStatus
import com.example.qlct.model.summary.searchTransactions
import com.example.qlct.model.summary.summarize
import com.example.qlct.viewmodel.state.ExpenseUiState
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val repository: ExpenseRepository, private val savedState: SavedStateHandle) : ViewModel() {
    private val month = savedState.getStateFlow("month", YearMonth.now().toString())
    private val monthOnly = savedState.getStateFlow("monthOnly", true)
    private val type = savedState.getStateFlow("type", "ALL")
    private val query = savedState.getStateFlow("query", "")
    private val retry = MutableStateFlow(0)
    private data class ActionState(
        val busy: Boolean = false,
        val error: String? = null,
        val completed: Long = 0
    )

    private val actionState = MutableStateFlow(ActionState())

    private val dataState = retry.flatMapLatest {
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
    }

    // The screen observes one immutable snapshot for data and action feedback.
    val uiState: StateFlow<ExpenseUiState> = combine(dataState, actionState) { data, action ->
        data.copy(
            busy = action.busy,
            actionError = action.error,
            completedActions = action.completed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    fun changeMonth(delta: Long) { savedState["month"] = YearMonth.parse(month.value).plusMonths(delta).toString() }
    fun setMonthOnly(value: Boolean) { savedState["monthOnly"] = value }
    fun setType(value: String) { savedState["type"] = value }
    fun setQuery(value: String) { savedState["query"] = value }
    fun saveBudget(month: String, amount: Long) = mutate { repository.saveBudget(month, amount) }
    fun deleteBudget(month: String) = mutate { repository.deleteBudget(month) }
    fun retryLoad() { retry.value++ }
    fun clearError() { actionState.update { it.copy(error = null) } }

    fun save(transaction: TransactionEntity) = mutate { repository.save(transaction) }
    fun delete(transaction: TransactionRow) = mutate { repository.delete(transaction.id) }

    private fun mutate(action: suspend () -> Unit) {
        if (actionState.value.busy) return
        actionState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
                actionState.update { it.copy(completed = it.completed + 1) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                actionState.update { it.copy(error = "Không thực hiện được. Dữ liệu nhập được giữ để thử lại.") }
            } finally {
                actionState.update { it.copy(busy = false) }
            }
        }
    }

    companion object {
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseViewModel(repository, createSavedStateHandle()) }
        }
    }
}
