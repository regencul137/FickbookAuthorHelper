package com.example.fickbookauthorhelper.logic.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface IFeedStorageSaver {
    fun saveFeed(feedJson: String)
    fun saveFeedLoadingDate(date: Long)
}

interface IFeedStorageProvider {
    fun getFeed(): String?

    /**
     * Return 0 if never saved
     */
    fun getFeedLoadingDate(): Long
}

class FeedStorage @Inject constructor(@ApplicationContext applicationContext: Context) :
    IFeedStorageSaver,
    IFeedStorageProvider {

    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    override fun saveFeed(feedJson: String) {
        sharedPreferences.edit().putString("feed", feedJson).apply()
    }

    override fun saveFeedLoadingDate(date: Long) {
        sharedPreferences.edit().putLong("feed_loading_date", date).apply()
    }

    override fun getFeed(): String? {
        return sharedPreferences.getString("feed", null)
    }

    override fun getFeedLoadingDate(): Long {
        return sharedPreferences.getLong("feed_loading_date", 0)
    }

    companion object {
        private const val PREFS_NAME = "FickbookFeedPrefs"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FeedStorageModule {
    @Provides
    @Singleton
    fun provideFeedStorage(@ApplicationContext context: Context): FeedStorage {
        return FeedStorage(context)
    }

    @Provides
    @Singleton
    fun provideIFeedStorageSaver(store: FeedStorage): IFeedStorageSaver = store

    @Provides
    @Singleton
    fun provideIFeedStorageProvider(store: FeedStorage): IFeedStorageProvider = store
}
