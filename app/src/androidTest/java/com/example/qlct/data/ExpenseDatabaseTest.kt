package com.example.qlct.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseDatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun legacyDatabaseMigratesAndCrudPersists() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { legacy ->
            legacy.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE)")
            legacy.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL CHECK(type IN ('INCOME','EXPENSE')), amount INTEGER NOT NULL CHECK(amount > 0 AND amount <= 999999999999), category_id INTEGER NOT NULL REFERENCES categories(id), date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '')")
            legacy.execSQL("CREATE INDEX transactions_date ON transactions(date)")
            legacy.execSQL("INSERT INTO categories VALUES (7,'Khác')")
            legacy.execSQL("INSERT INTO transactions VALUES (42,'EXPENSE',12345,7,'2026-01-31','Giữ dữ liệu cũ')")
            legacy.version = 1
        }
        fun open() = Room.databaseBuilder(context, ExpenseDatabase::class.java, name)
            .addMigrations(ExpenseDatabase.MIGRATION_1_2, ExpenseDatabase.MIGRATION_2_3, ExpenseDatabase.MIGRATION_3_4, ExpenseDatabase.MIGRATION_4_5).build()
        try {
            open().useDb { db ->
                val dao = db.dao()
                val migrated = dao.observeTransactions().first().single()
                assertEquals(42L, migrated.id)
                assertEquals("Giữ dữ liệu cũ", migrated.note)
                assertEquals(12345L, migrated.amount)
                assertEquals(7L, dao.observeCategories().first().single().id)
                val repository = ExpenseRepository(dao)
                repository.save(TransactionEntity(42, "INCOME", 22222, 7, "2026-02-01", " Sửa "))
                val id = dao.insert(TransactionEntity(type = "EXPENSE", amount = 10, categoryId = 7, date = LocalDate.now().toString()))
                assertTrue(id > 42)
                repository.delete(id)
                for (amount in listOf(-50000L, 0L, AmountValidator.MAX_AMOUNT + 1)) {
                    for (idToSave in listOf(0L, 42L)) {
                        try {
                            repository.save(TransactionEntity(idToSave, "EXPENSE", amount, 7, "2026-02-01"))
                            fail("Invalid amount must be rejected for insert and update")
                        } catch (_: IllegalArgumentException) { }
                    }
                }
                assertEquals(22222L, dao.observeTransactions().first().single().amount)
            }
            open().useDb { db ->
                val row = db.dao().observeTransactions().first().single()
                assertEquals("Sửa", row.note)
                assertEquals("INCOME", row.type)
                assertEquals(22222L, row.amount)
                db.dao().delete(row.id)
            }
            open().useDb { assertTrue(it.dao().observeTransactions().first().isEmpty()) }
        } finally { context.deleteDatabase(name) }
    }
}

private inline fun <T> ExpenseDatabase.useDb(block: (ExpenseDatabase) -> T): T =
    try { block(this) } finally { close() }
