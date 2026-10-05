package com.example.qlct

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Category(val id: Long, val name: String) {
    override fun toString() = name
}

data class Transaction(
    val id: Long = 0,
    val type: String,
    val amount: Long,
    val categoryId: Long,
    val date: String,
    val note: String,
    val categoryName: String = ""
)

class ExpenseDatabase(private val context: Context) :
    SQLiteOpenHelper(context, "qlct.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Runs only when the database is first created, never on every launch.
        context.assets.open("schema.sql").bufferedReader().use { it.readText() }
            .split(';').map { it.trim() }.filter { it.isNotEmpty() }
            .forEach { db.execSQL(it) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Cần bổ sung migration từ $oldVersion lên $newVersion")
    }

    fun categories(): List<Category> = readableDatabase.rawQuery(
        "SELECT id,name FROM categories ORDER BY id", null
    ).use { c -> buildList { while (c.moveToNext()) add(Category(c.getLong(0), c.getString(1))) } }

    fun transactions(): List<Transaction> = readableDatabase.rawQuery(
        "SELECT t.id,t.type,t.amount,t.category_id,t.date,t.note,c.name " +
            "FROM transactions t JOIN categories c ON c.id=t.category_id ORDER BY t.date DESC,t.id DESC", null
    ).use { c -> buildList {
        while (c.moveToNext()) add(Transaction(c.getLong(0),c.getString(1),c.getLong(2),
            c.getLong(3),c.getString(4),c.getString(5),c.getString(6)))
    } }

    fun save(t: Transaction) {
        require(t.type in listOf("INCOME", "EXPENSE"))
        require(t.amount in 1..999999999999L)
        val values = ContentValues().apply {
            put("type", t.type); put("amount", t.amount); put("category_id", t.categoryId)
            put("date", t.date); put("note", t.note.trim())
        }
        if (t.id == 0L) writableDatabase.insertOrThrow("transactions", null, values)
        else check(writableDatabase.update("transactions",values,"id=?",arrayOf(t.id.toString())) == 1)
    }

    fun delete(id: Long) {
        check(writableDatabase.delete("transactions", "id=?", arrayOf(id.toString())) == 1)
    }
}
