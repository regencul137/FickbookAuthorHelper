package com.example.fickbookauthorhelper.logic.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface ISecureStorageSaver {
    fun saveUsername(username: String)
    fun savePassword(password: String)
}

interface ISecureStorageProvider {
    fun getUsername(): String?
    fun getPassword(): String?
}

class SecureStorage @Inject constructor(@ApplicationContext applicationContext: Context) :
    ISecureStorageSaver,
    ISecureStorageProvider {
    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    override fun savePassword(password: String) {
        sharedPreferences.edit().putString("password", password).apply()
    }

    override fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    override fun getPassword(): String? {
        return sharedPreferences.getString("password", null)
    }

    companion object {
        private const val PREFS_NAME = "FickbookUserEncryptedPrefs"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SecureStorageModule {
    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage {
        return SecureStorage(context)
    }

    @Provides
    @Singleton
    fun provideISecureStorageSaver(store: SecureStorage): ISecureStorageSaver = store

    @Provides
    @Singleton
    fun provideISecureStorageProvider(store: SecureStorage): ISecureStorageProvider = store
}