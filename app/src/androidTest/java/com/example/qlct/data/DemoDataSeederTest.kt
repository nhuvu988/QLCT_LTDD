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
class DemoDataSeederTest {
    @Test fun demoPersistsWithoutDuplicatesAndOtherAccountsStayEmpty() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "demo-seeder-test.db"
        context.deleteDatabase(name)
        fun open() = Room.databaseBuilder(context, ExpenseDatabase::class.java, name)
            .addCallback(ExpenseDatabase.seedCallback(context)).build()
        var db = open()
        try {
            var auth = AuthRepository(db.accountDao(), DemoDataSeeder(db))
            assertTrue(auth.hasAccount())
            assertFalse(db.accountDao().find("vu98")!!.demoSeeded)
            assertTrue(db.dao().observeTransactions(db.accountDao().find("vu98")!!.id).first().isEmpty())
            val vu = auth.login("vu98", "12345678")!!
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            assertEquals(1345000L, db.dao().observeTransactions(vu.id).first().sumOf { it.amount })
            val other = auth.register("another", "12345678", "12345678")
            assertTrue(db.dao().observeTransactions(other.id).first().isEmpty())
            db.close()
            db = open()
            auth = AuthRepository(db.accountDao(), DemoDataSeeder(db))
            assertNotNull(auth.login("vu98", "12345678"))
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            assertNotNull(auth.login("vu98", "12345678"))
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            val first = db.dao().observeTransactions(vu.id).first().first()
            ExpenseRepository(db.dao(),vu.id).delete(first.id)
            auth.login("vu98", "12345678")
            assertEquals(5, db.dao().observeTransactions(vu.id).first().size)
            assertTrue(db.accountDao().find("vu98")!!.demoSeeded)
            try {
                db.dao().insert(TransactionEntity(type="EXPENSE",amount=1,categoryId=1,
                    date="2026-10-06",userId=99999))
                fail("Owner foreign key must reject nonexistent account")
            } catch (_: android.database.sqlite.SQLiteConstraintException) { }
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun sqlSeedNeverReplacesExistingPassword() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java).build()
        try {
            val regular = AuthRepository(db.accountDao())
            assertFalse(regular.hasAccount())
            val existing = regular.register("vu98", "different_password", "different_password")
            context.assets.open("seed.sql").bufferedReader().use { it.readText() }
                .split(';').map(String::trim).filter(String::isNotEmpty)
                .filter { it.startsWith("INSERT OR IGNORE INTO local_account") }
                .forEach(db.openHelper.writableDatabase::execSQL)
            val demo = AuthRepository(db.accountDao())
            assertEquals(existing.id, demo.login("vu98", "different_password")!!.id)
            assertNull(demo.login("vu98", "12345678"))
            assertEquals(existing.passwordHash, db.accountDao().find("vu98")!!.passwordHash)
        } finally { db.close() }
    }

    @Test fun existingInstallationGetsSeedAccountWithoutLosingOtherUsers() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "existing-account-seed-test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
        try {
            context.assets.open("seed.sql").bufferedReader().use { it.readText() }
                .split(';').map(String::trim).filter { it.startsWith("INSERT INTO categories") }
                .forEach(db.openHelper.writableDatabase::execSQL)
            val owner = AuthRepository(db.accountDao()).register("owner", "owner_password", "owner_password")
            ExpenseRepository(db.dao(), owner.id).save(TransactionEntity(type="EXPENSE",amount=12345,
                categoryId=1,date="2026-10-07",note="Keep this row"))
            db.close()
            db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name)
                .addCallback(ExpenseDatabase.seedCallback(context)).build()
            val auth = AuthRepository(db.accountDao(), DemoDataSeeder(db))
            assertNotNull(auth.login("owner", "owner_password"))
            val vu = auth.login("vu98", "12345678")!!
            assertEquals(6, db.dao().observeTransactions(vu.id).first().size)
            assertEquals(12345L, db.dao().observeTransactions(owner.id).first().single().amount)
            assertEquals(7,db.dao().observeCategories().first().size)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
