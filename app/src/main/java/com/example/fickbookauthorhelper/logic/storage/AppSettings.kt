package com.example.fickbookauthorhelper.logic.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface IAppSettingsSaver {
    fun setVpnEnabled(enabled: Boolean)
    fun clearProxy()
}

interface IAppSettingsProvider {
    fun isVpnEnabled(): Boolean
}

class AppSettings @Inject constructor(@ApplicationContext context: Context) :
    IAppSettingsSaver, IAppSettingsProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun setVpnEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_VPN_ENABLED, enabled).apply()
    }

    override fun isVpnEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VPN_ENABLED, false)
    }

    override fun clearProxy() {
        sharedPreferences.edit().remove(KEY_PROXY).apply()
    }

    companion object {
        private const val PREFS_NAME = "AppSettingsPrefs"
        private const val KEY_VPN_ENABLED = "vpn_enabled"
        private const val KEY_PROXY = "proxy"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppSettingsModule {
    @Singleton
    @Provides
    fun provideAppSettingsSaver(
        appSettings: AppSettings
    ): IAppSettingsSaver = appSettings

    @Singleton
    @Provides
    fun provideAppSettingsProvider(
        appSettings: AppSettings
    ): IAppSettingsProvider = appSettings
}
