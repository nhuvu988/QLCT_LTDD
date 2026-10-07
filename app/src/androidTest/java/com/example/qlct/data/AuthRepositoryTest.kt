package com.example.qlct.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {
    @Test fun registerLoginAndReopenWithoutStoringPlaintext() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "auth-integration-test.db"
        context.deleteDatabase(name)
        fun open() = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
        try {
            val db = open()
            try {
                val repository = AuthRepository(db.accountDao())
                assertFalse(repository.hasAccount())
                val first = repository.register(" Vu_123 ", "password1", "password1")
                assertEquals("vu_123", first.username)
                val stored = db.accountDao().account()!!
                assertNotEquals("password1", stored.passwordHash)
                assertEquals(64, stored.salt.length)
                assertEquals(64, stored.passwordHash.length)
                assertEquals("vu_123", repository.login("VU_123", "password1")?.username)
                assertNull(repository.login("vu_123", "incorrect"))
                assertNull(repository.login("other", "password1"))
                try {
                    repository.register("VU_123", "password2", "password2")
                    fail("Duplicate normalized username must be rejected")
                } catch (_: IllegalArgumentException) { }
                db.openHelper.writableDatabase.execSQL("INSERT INTO categories VALUES (7,'Khác')")
                val firstExpenses = ExpenseRepository(db.dao(), first.id)
                assertTrue(firstExpenses.transactions.first().isEmpty())
                firstExpenses.save(TransactionEntity(type="EXPENSE", amount=50000, categoryId=7, date="2026-10-06"))
                val row = firstExpenses.transactions.first().single()
                val second = repository.register("other", "password2", "password2")
                val secondExpenses = ExpenseRepository(db.dao(), second.id)
                assertTrue(secondExpenses.transactions.first().isEmpty())
                try { secondExpenses.delete(row.id); fail("Cannot delete another account's row") }
                catch (_: IllegalStateException) { }
                try { secondExpenses.save(TransactionEntity(row.id,"INCOME",100,7,"2026-10-06")); fail("Cannot edit another account's row") }
                catch (_: IllegalStateException) { }
                assertEquals(50000L, firstExpenses.transactions.first().single().amount)
                secondExpenses.save(TransactionEntity(type="INCOME",amount=20000,categoryId=7,date="2026-10-06"))
                assertEquals(20000L, secondExpenses.transactions.first().single().amount)
                assertEquals(50000L, firstExpenses.transactions.first().single().amount)
            } finally { db.close() }
            val reopened = open()
            try {
                val repository = AuthRepository(reopened.accountDao())
                assertTrue(repository.hasAccount())
                val loggedIn = repository.login("vu_123", "password1")!!
                assertEquals("vu_123", loggedIn.username)
                assertEquals(50000L, ExpenseRepository(reopened.dao(),loggedIn.id).transactions.first().single().amount)
            } finally { reopened.close() }
        } finally { context.deleteDatabase(name) }
    }
}
