package com.example.fickbookauthorhelper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FHApplication : Application() {
    companion object {
        const val FICKBOOK_URL = "https://ficbook.net"
        const val AVATAR_PATH = "user_data"
    }
}