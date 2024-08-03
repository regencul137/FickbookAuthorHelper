package com.example.fickbookauthorhelper

import android.content.Context
import androidx.annotation.DrawableRes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class FHAuthManager @Inject constructor(@ApplicationContext private val applicationContext: Context) : IAuthManager {
    override val currentUser: User? = null
    override val isSignedIn: Boolean
        get() = currentUser != null

    class User(val name: String, @DrawableRes val avatarId: Int)
}

interface IAuthManager {
    val currentUser: FHAuthManager.User?
    val isSignedIn: Boolean
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext applicationContext: Context): FHAuthManager {
        return FHAuthManager(applicationContext)
    }

    @Provides
    @Singleton
    fun provideIAuthManager(authManager: FHAuthManager): IAuthManager = authManager
}
