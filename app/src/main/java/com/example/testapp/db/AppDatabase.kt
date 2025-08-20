package com.example.testapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.testapp.dao.TrackDao
import com.example.testapp.entity.TrackEntity

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}