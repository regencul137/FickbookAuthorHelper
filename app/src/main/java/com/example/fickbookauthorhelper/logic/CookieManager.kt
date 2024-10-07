package com.example.fickbookauthorhelper.logic

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieManager @Inject constructor(@ApplicationContext applicationContext: Context) {
    private val sharedPreferences: SharedPreferences =
        applicationContext.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    fun saveCookies(cookies: String) {
        sharedPreferences.edit().putString("cookies", cookies).apply()
    }

    fun getCookies(): String? {
        return sharedPreferences.getString("cookies", null)
    }

    fun clearCookies() {
        sharedPreferences.edit().remove("cookies").apply()
    }
}
