package com.example.fickbookauthorhelper.logic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fickbookauthorhelper.logic.NotificationService
import com.example.fickbookauthorhelper.logic.feed.IFeedManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarkAsReadReceiver : BroadcastReceiver() {
    @Inject
    lateinit var feedManager: IFeedManager

    @Inject
    lateinit var notificationService: NotificationService

    override fun onReceive(context: Context, intent: Intent) {
        feedManager.saveFeedToStorage()

        val notificationId = intent.getIntExtra(NotificationService.PARAM_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            notificationService.cancelNotification(notificationId)
        }
    }
}