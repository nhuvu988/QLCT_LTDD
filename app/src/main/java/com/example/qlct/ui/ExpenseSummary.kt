package com.example.qlct.ui

import com.example.qlct.data.TransactionRow
import java.time.YearMonth

data class MonthlyExpense(val month: YearMonth, val amount: Long)
data class CategoryExpense(val id: Long, val name: String, val amount: Long)
data class ExpenseSummary(
    val income: Long,
    val expense: Long,
    val visible: List<TransactionRow>,
    val chart: List<MonthlyExpense>,
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

fun summarize(rows: List<TransactionRow>, month: YearMonth, monthOnly: Boolean, type: String): ExpenseSummary {
    val period = rows.filter { !monthOnly || it.date.startsWith(month.toString() + "-") }
    return ExpenseSummary(
        income = period.filter { it.type == "INCOME" }.sumOf { it.amount },
        expense = period.filter { it.type == "EXPENSE" }.sumOf { it.amount },
        visible = period.filter { type == "ALL" || it.type == type },
        chart = (5L downTo 0L).map { offset ->
            val item = month.minusMonths(offset)
            MonthlyExpense(item, rows.filter { it.type == "EXPENSE" && it.date.startsWith(item.toString() + "-") }.sumOf { it.amount })
        },
        categoryExpenses = period.filter { it.type == "EXPENSE" }.groupBy { it.categoryId }.map { (id, transactions) ->
            CategoryExpense(id, transactions.first().categoryName, transactions.sumOf { it.amount })
        }.sortedByDescending { it.amount }
    )
}
