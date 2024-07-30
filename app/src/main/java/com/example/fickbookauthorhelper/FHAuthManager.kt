package com.example.fickbookauthorhelper

import android.content.Context
import android.graphics.drawable.Drawable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class FHAuthManager @Inject constructor(@ApplicationContext private val applicationContext: Context) {
    val currentUser: User = User("Username", applicationContext.getDrawable(android.R.drawable.star_on))

    class User(val name: String, val avatar: Drawable?)
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
