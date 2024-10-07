package com.example.fickbookauthorhelper.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        sealed class Channel(val channelId: String, val notificationId: Int) {
            data object ProxyChannel : Channel("PROXY_CHANNEL", 1001)
        }
    }

    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        CoroutineScope(Dispatchers.Default).launch {
        }
    }

    fun configureNotificationChannels() {
        notificationManager.createNotificationChannel(buildProxyNotificationChannel())
    }

    private fun buildProxyNotificationChannel(): NotificationChannel {
        val name = applicationContext.getString(R.string.channel_name_proxies)
        val descriptionText = applicationContext.getString(R.string.channel_description_proxies, FICBOOK_URL)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        return NotificationChannel(Channel.ProxyChannel.channelId, name, importance).apply {
            description = descriptionText
        }
    }

    private fun showLoadProxyListNotification() {
        val builder = NotificationCompat.Builder(applicationContext, Channel.ProxyChannel.channelId)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle(applicationContext.getString(R.string.connection_corrupted))
            .setContentText(applicationContext.getString(R.string.loading_new_proxy_list))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)

        notificationManager.notify(Channel.ProxyChannel.notificationId, builder.build())
    }
}