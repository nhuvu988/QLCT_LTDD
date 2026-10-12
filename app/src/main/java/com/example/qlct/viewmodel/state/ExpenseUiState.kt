package com.example.qlct.viewmodel.state

import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.model.summary.BudgetStatus
import com.example.qlct.model.summary.ExpenseSummary
import java.time.YearMonth

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
    val budget: BudgetStatus = BudgetStatus(),
    val busy: Boolean = false,
    val actionError: String? = null,
    val completedActions: Long = 0
)
