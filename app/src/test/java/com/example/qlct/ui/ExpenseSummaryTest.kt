package com.example.qlct.ui

import com.example.qlct.data.TransactionRow
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class ExpenseSummaryTest {
    private fun row(id: Long, type: String, amount: Long, date: String) = TransactionRow(id, type, amount, 1, date, "", "Khác")

    @Test fun filtersDoNotChangeChartAndIncomeIsExcludedFromExpense() {
        val rows = listOf(row(1, "INCOME", 1000, "2026-01-01"), row(2, "EXPENSE", 200, "2026-01-31"),
            row(3, "EXPENSE", 300, "2025-12-31"), row(4, "EXPENSE", 999, "2025-01-01"))
        val result = summarize(rows, YearMonth.of(2026, 1), true, "INCOME")
        assertEquals(1000L, result.income)
        assertEquals(200L, result.expense)
        assertEquals(listOf(1L), result.visible.map { it.id })
        assertEquals(listOf(0L, 0L, 0L, 0L, 300L, 200L), result.chart.map { it.amount })
        assertEquals(YearMonth.of(2025, 8), result.chart.first().month)
        assertEquals(result.chart, summarize(rows, YearMonth.of(2026, 1), false, "EXPENSE").chart)
        assertEquals(1499L, summarize(rows, YearMonth.of(2026, 1), false, "ALL").expense)
    }

    @Test fun emptyMonthsAndLeapDayAreHandled() {
        val result = summarize(listOf(row(1, "EXPENSE", 999999999999, "2024-02-29")), YearMonth.of(2024, 2), true, "ALL")
        assertEquals(999999999999L, result.expense)
        assertEquals(6, result.chart.size)
        assertEquals(result.expense, result.chart.last().amount)
        assertTrue(summarize(emptyList(), YearMonth.of(2024, 2), true, "ALL").chart.all { it.amount == 0L })
    }

    @Test fun categoryChartGroupsExpensesAndRespectsPeriodButNotTypeFilter() {
        val rows = listOf(
            row(1, "EXPENSE", 100, "2026-10-01"),
            row(2, "EXPENSE", 200, "2026-10-02"),
            row(3, "EXPENSE", 500, "2026-10-02").copy(categoryId = 2, categoryName = "Ăn uống"),
            row(4, "INCOME", 9000, "2026-10-02"),
            row(5, "EXPENSE", 700, "2026-09-30")
        )
        val result = summarize(rows, YearMonth.of(2026, 10), true, "INCOME")
        assertEquals(listOf(500L, 300L), result.categoryExpenses.map { it.amount })
        assertEquals(result.expense, result.categoryExpenses.sumOf { it.amount })
        assertEquals(listOf(2L, 1L), result.categoryExpenses.map { it.id })
        assertEquals(1500L, summarize(rows, YearMonth.of(2026, 10), false, "ALL").categoryExpenses.sumOf { it.amount })
    }
}
