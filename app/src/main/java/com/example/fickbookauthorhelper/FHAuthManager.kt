package com.example.fickbookauthorhelper

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class FHAuthManager @Inject constructor(@ApplicationContext private val applicationContext: Context) {
    val currentUser: User = User("Username", R.drawable.ic_default_avatar)

    class User(val name: String, @DrawableRes val avatarId: Int)
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext applicationContext: Context): FHAuthManager {
        return FHAuthManager(applicationContext)
    }
}
