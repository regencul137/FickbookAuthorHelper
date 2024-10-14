package com.example.fickbookauthorhelper.logic.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.fickbookauthorhelper.logic.IEventProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieManager @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val eventProvider: IEventProvider
) {
    private val sharedPreferences: SharedPreferences =
        applicationContext.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            eventProvider.events.collectLatest {
                if (it is AuthManager.Event.SignedOut) {
                    clearCookies()
                }
            }
        }
    }

    fun saveCookies(cookies: String) {
        sharedPreferences.edit().putString("cookies", cookies).apply()
    }

    fun getCookies(): String? {
        return sharedPreferences.getString("cookies", null)
    }

    private fun clearCookies() {
        sharedPreferences.edit().remove("cookies").apply()
    }
}
