package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICKBOOK_URL
import com.example.fickbookauthorhelper.logic.FHEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface IHttpCheckSignInHelper {
    suspend fun isSignedIn(): Result<Boolean>
}

class HttpCheckSignInHelper @Inject constructor(
    private val clientProvider: IHttpClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpCheckSignInHelper {
    override suspend fun isSignedIn(): Result<Boolean> {
        val userBlockId = "profile-holder"

        val request = Request.Builder()
            .url(FICKBOOK_URL)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.success(false)
                    Result.success(body.contains(userBlockId))
                } else {
                    eventEmitter.emit(FHEvent.CheckSignInSuccess)
                    Result.success(false)
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object CheckSignInModule {
    @Singleton
    @Provides
    fun provideCheckSignInHelper(
        clientProvider: IHttpClientProvider,
        eventEmitter: IEventEmitter
    ): HttpCheckSignInHelper {
        return HttpCheckSignInHelper(clientProvider, eventEmitter)
    }

    @Singleton
    @Provides
    fun provideISignInHelper(checkSignInHelper: HttpCheckSignInHelper): IHttpCheckSignInHelper = checkSignInHelper
}