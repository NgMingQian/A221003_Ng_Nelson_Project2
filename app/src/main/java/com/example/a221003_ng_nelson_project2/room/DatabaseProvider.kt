package com.example.a221003_ng_nelson_project2.room

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "weather_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}