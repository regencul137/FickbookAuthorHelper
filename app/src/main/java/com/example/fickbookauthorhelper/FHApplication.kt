package com.example.fickbookauthorhelper

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.fickbookauthorhelper.logic.NotificationService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FHApplication : Application(), Configuration.Provider {
    companion object {
        const val FICBOOK_URL = "https://ficbook.net"
        const val AVATAR_PATH = "user_data"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationService: NotificationService

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationService.configureNotificationChannels()
    }
}