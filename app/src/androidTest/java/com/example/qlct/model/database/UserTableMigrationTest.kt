package com.example.qlct.model.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.qlct.model.entity.User
import com.example.qlct.model.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserTableMigrationTest {
    private inline fun <T> ExpenseDatabase.useDb(block: (ExpenseDatabase) -> T): T =
        try { block(this) } finally { close() }

    @Test fun renamePreservesUsersTransactionsBudgetsAndForeignKeys() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (empty in listOf(false, true)) {
            val name = "users-migration-$empty.db"
            context.deleteDatabase(name)
            val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
            SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
                db.execSQL("CREATE TABLE categories (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
                db.execSQL("CREATE TABLE local_account (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL, demoSeeded INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE UNIQUE INDEX index_local_account_username ON local_account(username)")
                db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, category_id INTEGER NOT NULL, date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '', user_id INTEGER, FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(user_id) REFERENCES local_account(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX transactions_date ON transactions(date)")
                db.execSQL("CREATE INDEX index_transactions_category_id ON transactions(category_id)")
                db.execSQL("CREATE INDEX index_transactions_user_id ON transactions(user_id)")
                db.execSQL("CREATE TABLE budgets (user_id INTEGER NOT NULL, month TEXT NOT NULL, amount INTEGER NOT NULL, PRIMARY KEY(user_id, month), FOREIGN KEY(user_id) REFERENCES local_account(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                if (!empty) {
                    db.execSQL("INSERT INTO categories VALUES (1,'Food')")
                    db.execSQL("INSERT INTO local_account VALUES (7,'existing','saved-salt','saved-hash',1)")
                    db.execSQL("INSERT INTO transactions VALUES (42,'EXPENSE',45000,1,'2026-10-21','Keep me',7)")
                    db.execSQL("INSERT INTO transactions VALUES (9000,'EXPENSE',1,1,'2026-10-21','Deleted',7)")
                    db.execSQL("DELETE FROM transactions WHERE id=9000")
                    db.execSQL("INSERT INTO budgets VALUES (7,'2026-10',2000000)")
                }
                db.version = 6
            }
            fun open() = Room.databaseBuilder(context, ExpenseDatabase::class.java, name)
                .addMigrations(ExpenseDatabase.MIGRATION_6_7).build()
            try {
                open().useDb { db ->
                    assertEquals(if (empty) 0 else 1, db.dao().observeCategories().first().size)
                    if (!empty) {
                        val user = db.accountDao().find("existing")!!
                        assertEquals(7, user.id)
                        assertEquals("saved-salt", user.salt)
                        assertEquals("saved-hash", user.passwordHash)
                        assertTrue(user.demoSeeded)
                        assertEquals(42L, db.dao().observeTransactions(7).first().single().id)
                        assertEquals(2000000L, db.dao().observeBudgets(7).first().single().amount)
                        val id = db.dao().insert(TransactionEntity(type="INCOME", amount=10,
                            categoryId=1, date="2026-10-21", userId=7))
                        assertTrue(id > 9000)
                        try {
                            db.dao().insert(TransactionEntity(type="EXPENSE", amount=1,
                                categoryId=1, date="2026-10-21", userId=999))
                            fail("Missing user accepted")
                        } catch (_: android.database.sqlite.SQLiteConstraintException) { }
                    }
                    db.openHelper.writableDatabase.query("PRAGMA foreign_key_check").use { assertEquals(0, it.count) }
                    db.openHelper.writableDatabase.query("SELECT name FROM sqlite_master WHERE type='table' AND name='local_account'").use { assertEquals(0, it.count) }
                    val id = db.accountDao().create(User(username="new_user",salt="s",passwordHash="h"))
                    assertTrue(id > if (empty) 0 else 7)
                }
                open().useDb { db -> assertNotNull(db.accountDao().find("new_user")) }
            } finally { context.deleteDatabase(name) }
        }
    }
}
