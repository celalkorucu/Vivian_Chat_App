package com.celalkorucu.xxx.Manager

import android.database.sqlite.SQLiteDatabase


class DatabaseManager  private constructor(){


    private var currentDatabase: SQLiteDatabase? = null

    fun setCurrentDatabase(sqliteDatabase: SQLiteDatabase) {
        currentDatabase = sqliteDatabase
    }

    fun getCurrentDatabase(): SQLiteDatabase? {
        return currentDatabase
    }

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager().also { instance = it }
            }
        }
    }
}
