package com.example.qlct.ui

import com.example.qlct.data.TransactionRow
import java.text.Normalizer
import java.util.Locale

enum class BudgetLevel { NONE, NORMAL, WARNING, REACHED, EXCEEDED }

data class BudgetStatus(val limit: Long? = null, val spent: Long = 0) {
    val remaining: Long? get() = limit?.minus(spent)
    val ratio: Float get() = if (limit != null && limit > 0) spent.toFloat() / limit else 0f
    val progress: Float get() = ratio.coerceIn(0f, 1f)
    val level: BudgetLevel get() = when {
        limit == null || limit <= 0 -> BudgetLevel.NONE
        spent > limit -> BudgetLevel.EXCEEDED
        spent == limit -> BudgetLevel.REACHED
        spent.toDouble() / limit >= 0.8 -> BudgetLevel.WARNING
        else -> BudgetLevel.NORMAL
    }
}

private fun normalizeSearch(value: String): String =
    Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace('đ', 'd')

/** All words must match the note/category; do not alter dashboard totals. */
fun searchTransactions(rows: List<TransactionRow>, query: String): List<TransactionRow> {
    val words = normalizeSearch(query.trim()).split(Regex("\\s+")).filter(String::isNotEmpty)
    if (words.isEmpty()) return rows
    return rows.filter { row ->
        val text = normalizeSearch("${row.note} ${row.categoryName}")
        words.all(text::contains)
    }
}
