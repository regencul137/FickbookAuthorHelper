package com.example.fickbookauthorhelper.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.fickbookauthorhelper.MainActivity
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.feed.FeedManager
import com.example.fickbookauthorhelper.logic.feed.FeedType
import com.example.fickbookauthorhelper.logic.receivers.MarkAsReadReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val lifecycleObserver: AppLifecycleObserver,
    private val eventProvider: IEventProvider
) {
    companion object {
        sealed class Channel(val channelId: String) {
            data object FeedChannel : Channel("FEED_CHANNEL")
        }

        const val PARAM_NOTIFICATION_ID = "PARAM_NOTIFICATION_ID"
    }

    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        CoroutineScope(Dispatchers.Default).launch {
            eventProvider.events.collectLatest {
                when (it) {
                    is FeedManager.Event.FeedUpdated -> {
                        if (lifecycleObserver.appInBackground) {
                            showNotificationsForFeed(it.gainFeed)
                        }
                    }
                }
            }
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun configureNotificationChannels() {
        notificationManager.createNotificationChannel(buildFeedNotificationChannel())
    }

    private fun showNotificationsForFeed(feed: FeedManager.GainFeed) {
        feed.stats.forEach { stat ->
            stat.value?.forEach {
                if (it.gain > 0) {
                    showNotificationsForFeedStat(stat.key, it)
                }
            }
        }
    }

    private fun showNotificationsForFeedStat(type: FeedType, stat: FeedManager.GainFeed.GainFicStat) {
        val title = when (type) {
            FeedType.KUDOS -> applicationContext.getString(R.string.new_kudos_for, stat.ficName)
            FeedType.REVIEWS -> applicationContext.getString(R.string.new_reviews_for, stat.ficName)
            FeedType.SUBSCRIPTIONS -> applicationContext.getString(R.string.new_subscriptions_for, stat.ficName)
            FeedType.VIEWS -> applicationContext.getString(R.string.new_views_for, stat.ficName)
            FeedType.WAITS -> applicationContext.getString(R.string.new_waits_for, stat.ficName)
        }

        val notificationId = (type.toString() + stat.ficName).hashCode()
        val text = applicationContext.getString(R.string.notification_text_feed, stat.gain, stat.stat)

        val markAsReadIntent = Intent(applicationContext, MarkAsReadReceiver::class.java).apply {
            putExtra(PARAM_NOTIFICATION_ID, notificationId)
        }
        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            markAsReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(applicationContext, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, Channel.FeedChannel.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                android.R.drawable.checkbox_on_background,
                applicationContext.getString(R.string.mark_as_read),
                markAsReadPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_set_as,
                applicationContext.getString(R.string.open_the_app),
                openAppPendingIntent
            )
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(markAsReadPendingIntent)
            .setAutoCancel(false)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun buildFeedNotificationChannel(): NotificationChannel {
        val name = applicationContext.getString(R.string.channel_name_feed)
        val descriptionText = applicationContext.getString(R.string.channel_feed_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        return NotificationChannel(Channel.FeedChannel.channelId, name, importance).apply {
            description = descriptionText
        }
    }
}