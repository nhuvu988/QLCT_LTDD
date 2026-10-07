package com.example.qlct.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Populate fresh and existing installations without resetting user data. */
class DatabaseSeedCallback(context: Context) : RoomDatabase.Callback() {
    private val appContext = context.applicationContext

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        val statements = appContext.assets.open("seed.sql").bufferedReader().use { reader ->
            reader.readLines().filterNot { it.trimStart().startsWith("--") }.joinToString("\n")
        }.split(';').map(String::trim).filter(String::isNotEmpty)
        db.beginTransaction()
        try {
            statements.forEach(db::execSQL)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
