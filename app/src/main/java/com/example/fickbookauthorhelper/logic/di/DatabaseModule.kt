package com.example.fickbookauthorhelper.logic.di

import android.content.Context
import androidx.room.Room
import com.example.fickbookauthorhelper.logic.room.AppDatabase
import com.example.fickbookauthorhelper.logic.room.daos.FanficDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database"
        ).build()
    }

    @Provides
    fun provideFanficDao(database: AppDatabase): FanficDao {
        return database.fanficDao()
    }
}