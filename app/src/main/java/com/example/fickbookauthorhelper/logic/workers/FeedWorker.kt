package com.example.fickbookauthorhelper.logic.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fickbookauthorhelper.logic.feed.FeedManager
import com.example.fickbookauthorhelper.logic.feed.IFeedManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@HiltWorker
class FeedWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParameters: WorkerParameters,
    private val feedManager: IFeedManager
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val WORK_NAME = "FEED_WORK"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                feedManager.updateFeedFromSite()
                Result.retry()
            } catch (e: Exception) {
                println("ProxyWorker failed: ${e.cause}")
                Result.failure()
            }
        }
    }
}
