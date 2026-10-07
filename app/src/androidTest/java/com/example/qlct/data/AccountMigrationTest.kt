package com.example.qlct.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountMigrationTest {
    @Test fun migrationKeepsPersonalDataAndSeparatesOldSamples() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "account-migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        // Exact v3 schema, with an existing device account and both kinds of rows.
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE categories (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
            db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, category_id INTEGER NOT NULL, date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '', FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            db.execSQL("CREATE INDEX transactions_date ON transactions(date)")
            db.execSQL("CREATE INDEX index_transactions_category_id ON transactions(category_id)")
            db.execSQL("CREATE TABLE local_account (id INTEGER NOT NULL PRIMARY KEY, username TEXT NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL)")
            db.execSQL("INSERT INTO categories VALUES (1,'Ăn uống')")
            db.execSQL("INSERT INTO local_account VALUES (1,'existing','salt','hash')")
            db.execSQL("INSERT INTO transactions VALUES (1,'EXPENSE',45000,1,'2026-10-06','Dữ liệu mẫu: ăn trưa')")
            db.execSQL("INSERT INTO transactions VALUES (2,'EXPENSE',60000,1,'2026-10-06','Giao dịch cá nhân')")
            db.version = 3
        }
        val db = Room.databaseBuilder(context,ExpenseDatabase::class.java,name)
            .addMigrations(ExpenseDatabase.MIGRATION_3_4, ExpenseDatabase.MIGRATION_4_5, ExpenseDatabase.MIGRATION_5_6).build()
        try {
            assertEquals("existing",db.accountDao().account()!!.username)
            assertEquals(2L,db.dao().observeTransactions(1).first().single().id)
            assertEquals(1L,db.dao().observeTransactions().first().single().id)
            val second = AuthRepository(db.accountDao()).register("new_user","password1","password1")
            assertTrue(second.id > 1)
            assertTrue(db.dao().observeTransactions(second.id).first().isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun seedContainsCategoriesAndNoTransactions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SQLiteDatabase.create(null).use { db ->
            db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY, name TEXT NOT NULL)")
            db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY)")
            db.execSQL("CREATE TABLE local_account (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL, demoSeeded INTEGER NOT NULL DEFAULT 0)")
            context.assets.open("seed.sql").bufferedReader().use { it.readText() }
                .split(';').map(String::trim).filter(String::isNotEmpty).forEach(db::execSQL)
            db.rawQuery("SELECT COUNT(*) FROM categories",null).use { it.moveToFirst(); assertEquals(7,it.getInt(0)) }
            db.rawQuery("SELECT COUNT(*) FROM transactions",null).use { it.moveToFirst(); assertEquals(0,it.getInt(0)) }
        }
    }
}
