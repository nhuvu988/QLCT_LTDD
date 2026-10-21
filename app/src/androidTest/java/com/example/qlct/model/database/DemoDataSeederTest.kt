package com.example.qlct.model.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.repository.AuthRepository
import com.example.qlct.model.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoDataSeederTest {
    @Test fun seedExistingDatabasePreservesAccountPasswordAndOwnedRows() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "seed-existing-account-test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
        try {
            val original = AuthRepository(db.accountDao()).register("vu98", "old-password", "old-password")
            db.dao().insertCategoriesIfMissing(listOf(Category(7, "Other")))
            ExpenseRepository(db.dao(), original.id).save(
                TransactionEntity(type="INCOME", amount=123456, categoryId=7, date="2026-10-07"))
            db.close()
            db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name)
                .build()
            val auth = AuthRepository(db.accountDao(), initializer = DatabaseInitializer(db))
            assertTrue(auth.hasAccount())
            DatabaseInitializer(db).initialize()
            assertEquals(7, db.dao().observeCategories().first().size)
            assertEquals("Other", db.dao().observeCategories().first().single { it.id == 7L }.name)
            assertEquals(original.id, auth.login("vu98", "old-password")!!.id)
            assertNull(auth.login("vu98", "11111111"))
            assertEquals(123456L, db.dao().observeTransactions(original.id).first().single().amount)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun demoPersistsWithoutDuplicatesAndOtherAccountsStayEmpty() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "demo-seeder-test.db"
        context.deleteDatabase(name)
        fun open() = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
        var db = open()
        try {
            var auth = AuthRepository(db.accountDao(), DemoDataSeeder(db), DatabaseInitializer(db))
            val vu = auth.login("vu98", "11111111")!!
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            assertEquals(1345000L, db.dao().observeTransactions(vu.id).first().sumOf { it.amount })
            val other = auth.register("another", "12345678", "12345678")
            assertTrue(db.dao().observeTransactions(other.id).first().isEmpty())
            db.close()
            db = open()
            auth = AuthRepository(db.accountDao(), DemoDataSeeder(db), DatabaseInitializer(db))
            assertNotNull(auth.login("vu98", "11111111"))
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            assertNotNull(auth.login("vu98", "11111111"))
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            val first = db.dao().observeTransactions(vu.id).first().first()
            ExpenseRepository(db.dao(),vu.id).delete(first.id)
            auth.login("vu98", "11111111")
            assertEquals(5, db.dao().observeTransactions(vu.id).first().size)
            assertTrue(db.accountDao().find("vu98")!!.demoSeeded)
            try {
                db.dao().insert(TransactionEntity(type="EXPENSE",amount=1,categoryId=1,
                    date="2026-10-06",userId=99999))
                fail("Owner foreign key must reject nonexistent account")
            } catch (_: android.database.sqlite.SQLiteConstraintException) { }
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
