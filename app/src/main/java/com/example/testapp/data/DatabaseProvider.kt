package com.example.testapp.data

import android.content.Context
import androidx.room.Room
import com.example.testapp.db.AppDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun init(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.Companion.DATABASE_NAME
            ).build()
            INSTANCE = instance
            instance
        }
    }

    fun getAppDatabase(): AppDatabase {
        return INSTANCE ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }
}