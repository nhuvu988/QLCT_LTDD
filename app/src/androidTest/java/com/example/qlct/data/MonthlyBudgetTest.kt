package com.example.qlct.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MonthlyBudgetTest {
    @Test fun budgetsPersistAndAreIsolatedByAccountAndMonth() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "monthly-budget-test.db"
        context.deleteDatabase(name)
        fun open() = Room.databaseBuilder(context,ExpenseDatabase::class.java,name).build()
        var db = open()
        try {
            val a = db.accountDao().create(LocalAccount(username="one",salt="s",passwordHash="h")).toInt()
            val b = db.accountDao().create(LocalAccount(username="two",salt="s",passwordHash="h")).toInt()
            var first = ExpenseRepository(db.dao(),a)
            val second = ExpenseRepository(db.dao(),b)
            first.saveBudget("2026-10",1000)
            first.saveBudget("2026-10",2000)
            first.saveBudget("2026-11",3000)
            second.saveBudget("2026-10",9000)
            assertEquals(2, first.budgets.first().size)
            assertEquals(9000L, second.budgets.first().single().amount)
            for (invalid in listOf(0L,-1L,1000000000000L)) {
                try { first.saveBudget("2026-10",invalid); fail("Invalid budget accepted") }
                catch (_: IllegalArgumentException) { }
            }
            try { first.saveBudget("2026-13",1000); fail("Invalid month accepted") }
            catch (_: java.time.format.DateTimeParseException) { }
            db.close()
            db=open()
            first=ExpenseRepository(db.dao(),a)
            assertEquals(2000L,first.budgets.first().first { it.month=="2026-10" }.amount)
            first.deleteBudget("2026-10")
            assertEquals("2026-11",first.budgets.first().single().month)
            assertEquals(9000L,ExpenseRepository(db.dao(),b).budgets.first().single().amount)
            try { db.dao().saveBudget(MonthlyBudget(99999,"2026-10",100)); fail("Missing account accepted") }
            catch (_: android.database.sqlite.SQLiteConstraintException) { }
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
