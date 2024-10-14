package com.example.fickbookauthorhelper.logic.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fickbookauthorhelper.logic.room.daos.FanficDao
import com.example.fickbookauthorhelper.logic.room.entities.Fanfic

@Database(entities = [Fanfic::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fanficDao(): FanficDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fanfic_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}