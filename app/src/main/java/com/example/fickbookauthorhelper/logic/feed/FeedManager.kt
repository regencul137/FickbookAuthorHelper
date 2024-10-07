package com.example.fickbookauthorhelper.logic.feed

import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.feed.FeedManager.GainFeed
import com.example.fickbookauthorhelper.logic.http.IHttpFeedLoader
import com.example.fickbookauthorhelper.logic.http.IHttpFeedLoader.FicStat
import com.example.fickbookauthorhelper.logic.storage.IFeedStorageProvider
import com.example.fickbookauthorhelper.logic.storage.IFeedStorageSaver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface IFeedManager {
    suspend fun updateFeedFromSite()
    fun saveFeedToStorage()
    val feedFlow: Flow<GainFeed?>
    val lastUpdateTimeFlow: Flow<Long?>
}

@Singleton
class FeedManager @Inject constructor(
    private val feedLoader: IHttpFeedLoader,
    private val feedStorageSaver: IFeedStorageSaver,
    private val feedStorageProvider: IFeedStorageProvider,
    private val coroutineScope: CoroutineScope
) : IFeedManager {
    companion object {
        private const val UPDATE_INTERVAL = 10000L
    }

    private val _feedFlow = MutableStateFlow<GainFeed?>(null)
    override val feedFlow: Flow<GainFeed?> get() = _feedFlow
    private var latestFeedFromSite: IHttpFeedLoader.Feed? = null

    private val _lastUpdateTimeFlow = MutableStateFlow<Long?>(null)
    override val lastUpdateTimeFlow: Flow<Long?> get() = _lastUpdateTimeFlow

    init {
        updateFeedFromStore()
        startAutomaticFeedUpdate()
    }

    override suspend fun updateFeedFromSite() {
        withContext(Dispatchers.IO) {
            feedLoader.loadFeed().onSuccess { feed ->
                feed?.let {
                    if (feedStorageProvider.getFeed() == null) {
                        println("FeedManager Save feed for the first time")
                        saveFeed(it)
                    } else {
                        val dateMillis = feedStorageProvider.getFeedLoadingDate()
                        val date = Instant.ofEpochMilli(dateMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val today = LocalDate.now()
                        if (!date.isEqual(today)) {
                            println("FeedManager Saved feed was expired")
                            saveFeed(it)
                        }
                    }
                    _lastUpdateTimeFlow.value = System.currentTimeMillis()
                    updateGainFeed(feed)
                }
            }
        }
    }

    override fun saveFeedToStorage() {
        latestFeedFromSite?.let { saveFeed(it) }
    }

    private fun updateGainFeed(feed: IHttpFeedLoader.Feed) {
        latestFeedFromSite = feed
        feedFromStore()?.let { latestFeed ->
            val gainFeedStats = feed.stats.mapValues { (key, newFicStats) ->
                val latestFicStats = latestFeed.stats[key]
                newFicStats?.map { newFicStat ->
                    val latestStat = latestFicStats?.find { it.ficName == newFicStat.ficName }?.stat ?: 0
                    val gain = newFicStat.stat - latestStat
                    GainFeed.GainFicStat(gain = gain, ficStat = newFicStat)
                }
            }
            _feedFlow.value = GainFeed(stats = gainFeedStats)
        } ?: run {
            updateWithNoGain(feed)
        }
    }

    private fun updateWithNoGain(feed: IHttpFeedLoader.Feed) {
        val gainFeedStats = feed.stats.mapValues { (_, ficStats) ->
            ficStats?.map { ficStat ->
                GainFeed.GainFicStat(gain = 0, ficStat = ficStat)
            }
        }
        _feedFlow.value = GainFeed(stats = gainFeedStats)
    }

    private fun updateFeedFromStore() {
        feedFromStore()?.let {
            updateWithNoGain(it)
            _lastUpdateTimeFlow.value = feedStorageProvider.getFeedLoadingDate()
        }
    }

    private fun feedFromStore(): IHttpFeedLoader.Feed? {
        val storedFeedJson = feedStorageProvider.getFeed()
        return storedFeedJson?.let { deserializeFeed(it) } ?: run { null }
    }

    private fun saveFeed(feed: IHttpFeedLoader.Feed) {
        println("FeedManager saveFeed to Storage")
        val feedJson = serializeFeed(feed)
        val currentTime = System.currentTimeMillis()
        feedStorageSaver.saveFeed(feedJson)
        feedStorageSaver.saveFeedLoadingDate(currentTime)
    }

    private fun startAutomaticFeedUpdate() {
        coroutineScope.launch {
            while (isActive) {
                updateFeedFromSite()
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private fun serializeFeed(feed: IHttpFeedLoader.Feed): String {
        val rawStats = feed.stats.mapKeys { (key, _) ->
            FeedType.toString(key)
        }
        return Gson().toJson(rawStats)
    }

    private fun deserializeFeed(feedJson: String): IHttpFeedLoader.Feed {
        val feedType = object : TypeToken<Map<String, List<FicStat>?>>() {}.type
        val rawStats: Map<String, List<FicStat>?> = Gson().fromJson(feedJson, feedType)
        val convertedStats = rawStats.mapKeys { (key, _) ->
            FeedType.fromString(key)
        }
        return IHttpFeedLoader.Feed(stats = convertedStats)
    }

    data class GainFeed(
        val stats: Map<FeedType, List<GainFicStat>?>
    ) {
        data class GainFicStat(val gain: Int, private val ficStat: FicStat) {
            val ficName: String
                get() = ficStat.ficName
            val stat: Int
                get() = ficStat.stat
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {
    @Provides
    @Singleton
    fun provideFeedManager(
        feedLoader: IHttpFeedLoader,
        feedStorageSaver: IFeedStorageSaver,
        feedStorageProvider: IFeedStorageProvider
    ): IFeedManager {
        return FeedManager(feedLoader, feedStorageSaver, feedStorageProvider, CoroutineScope(Dispatchers.IO))
    }
}
