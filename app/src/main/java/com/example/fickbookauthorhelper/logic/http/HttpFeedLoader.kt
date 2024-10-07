package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.feed.FeedType
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import javax.inject.Inject

interface IHttpFeedLoader {
    suspend fun loadFeed(): Result<Feed?>

    data class Feed(
        val stats: Map<FeedType, List<FicStat>?>
    )

    data class FicStat(val ficName: String, val stat: Int)
}

class HttpFeedLoader @Inject constructor(
    private val clientProvider: IClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpFeedLoader {
    sealed class Event : IEvent {
        data object FeedLoadingStarted : Event()
        data class FeedLoadingEnded(val feed: IHttpFeedLoader.Feed?) : Event()
    }

    override suspend fun loadFeed(): Result<IHttpFeedLoader.Feed?> {
        eventEmitter.pushEvent(Event.FeedLoadingStarted)

        val request = Request.Builder()
            .url("$FICBOOK_URL/home/news")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.success(null)
                    val feed = parseFeed(body)
                    eventEmitter.pushEvent(Event.FeedLoadingEnded(feed))
                    Result.success(feed)
                } else {
                    eventEmitter.pushEvent(Event.FeedLoadingEnded(null))
                    Result.success(null)
                }
            } catch (e: IOException) {
                eventEmitter.pushEvent(Event.FeedLoadingEnded(null))
                Result.failure(e)
            }
        }
    }

    private fun parseFeed(html: String): IHttpFeedLoader.Feed {
        val doc: Document = Jsoup.parse(html)

        val statTypes = mapOf(
            FeedType.VIEWS to "ic_file-eye",
            FeedType.KUDOS to "ic_thumbs-up",
            FeedType.SUBSCRIPTIONS to "ic_star-full",
            FeedType.WAITS to "ic_file-check",
            FeedType.REVIEWS to "ic_bubble-dark "
        )

        val stats = statTypes.mapValues { (_, iconClass) -> parseStats(doc, iconClass) }

        return IHttpFeedLoader.Feed(stats = stats)
    }

    private fun parseStats(doc: Document, iconClass: String): List<IHttpFeedLoader.FicStat>? {
        return doc.select("div.news-subheader:has(svg.$iconClass)").firstOrNull()?.let { header ->
            header.nextElementSibling()?.select("li")?.map { li ->
                val ficName = li.selectFirst("a")?.text().orEmpty()
                val stat = li.selectFirst("strong")?.text()?.toIntOrNull() ?: 0
                IHttpFeedLoader.FicStat(ficName, stat)
            }
        }
    }
}