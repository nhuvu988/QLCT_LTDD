package com.example.qlct.ui

import com.example.qlct.data.TransactionRow
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class BudgetAndSearchTest {
    private val rows = listOf(
        TransactionRow(1,"EXPENSE",800,1,"2026-10-01","Ăn trưa ở trường","Ăn uống"),
        TransactionRow(2,"EXPENSE",150,2,"2026-09-30","Đổ xăng","Di chuyển"),
        TransactionRow(3,"INCOME",5000,3,"2026-10-02","Lương tháng","Lương"))

    @Test fun warningBoundariesAndProgress() {
        assertEquals(BudgetLevel.NONE, BudgetStatus(null,800).level)
        assertEquals(BudgetLevel.NORMAL, BudgetStatus(1000,799).level)
        assertEquals(BudgetLevel.WARNING, BudgetStatus(1000,800).level)
        assertEquals(BudgetLevel.REACHED, BudgetStatus(1000,1000).level)
        val exceeded = BudgetStatus(1000,1200)
        assertEquals(BudgetLevel.EXCEEDED, exceeded.level)
        assertEquals(-200L, exceeded.remaining)
        assertEquals(1f, exceeded.progress, 0f)
        assertEquals(0f, BudgetStatus(1000,0).progress, 0f)
    }

    @Test fun searchIgnoresAccentsCaseAndExtraSpaces() {
        assertEquals(listOf(1L), searchTransactions(rows,"  AN   truong ").map { it.id })
        assertEquals(listOf(2L), searchTransactions(rows,"ĐỔ XĂNG").map { it.id })
        assertEquals(listOf(2L), searchTransactions(rows,"do chuyen").map { it.id })
        assertEquals(listOf(3L), searchTransactions(rows,"luong").map { it.id })
        assertEquals(rows, searchTransactions(rows,"  "))
        assertTrue(searchTransactions(rows,"khong ton tai").isEmpty())
        assertTrue(searchTransactions(rows,"%'").isEmpty())
    }

    @Test fun searchCombinesWithMonthAndTypeWithoutChangingTotalsOrBudget() {
        val october = YearMonth.of(2026,10)
        val summary = summarize(rows,october,true,"EXPENSE")
        assertEquals(listOf(1L), searchTransactions(summary.visible,"an").map { it.id })
        assertTrue(searchTransactions(summary.visible,"luong").isEmpty())
        assertEquals(5000L, summary.income)
        assertEquals(800L, summary.expense)
        val allIncome = summarize(rows,october,false,"INCOME")
        assertEquals(800L, allIncome.chart.last().amount)
        assertEquals(200L, BudgetStatus(1000,allIncome.chart.last().amount).remaining)
    }

    @Test fun budgetTracksTransactionEditsAndDeletes() {
        val month = YearMonth.of(2026,10)
        fun spent(items: List<TransactionRow>) = summarize(items,month,false,"ALL").chart.last().amount
        assertEquals(800L, spent(rows))
        assertEquals(1200L, spent(rows.map { if (it.id==1L) it.copy(amount=1200) else it }))
        assertEquals(0L, spent(rows.filterNot { it.id==1L }))
        assertEquals(0L, spent(rows.map { if (it.id==1L) it.copy(date="2026-11-01") else it }))
    }
}
