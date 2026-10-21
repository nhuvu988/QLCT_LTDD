package com.example.qlct.model.database

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInitializerTest {
    @Test fun concurrentInitializationIsIdempotentAndDoesNotSeedTransactions() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java).build()
        try {
            coroutineScope {
                List(3) { async { DatabaseInitializer(db).initialize() } }.awaitAll()
            }
            val account = requireNotNull(db.accountDao().find("vu98"))
            assertEquals(7, db.dao().observeCategories().first().size)
            assertFalse(account.demoSeeded)
            assertTrue(db.dao().observeTransactions(account.id).first().isEmpty())
            DatabaseInitializer(db).initialize()
            assertEquals(account, db.accountDao().find("vu98"))
        } finally {
            db.close()
        }
    }

    @Test fun demoCompletionFlagRollsBackWithRoomTransaction() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java).build()
        try {
            DatabaseInitializer(db).initialize()
            val account = requireNotNull(db.accountDao().find("vu98"))
            try {
                db.withTransaction {
                    db.accountDao().markDemoSeeded(account.id)
                    error("Simulate interrupted seeding")
                }
            } catch (_: IllegalStateException) {
                // A failed transaction must not mark the demo as complete.
            }
            assertFalse(requireNotNull(db.accountDao().find("vu98")).demoSeeded)
        } finally {
            db.close()
        }
    }
}
