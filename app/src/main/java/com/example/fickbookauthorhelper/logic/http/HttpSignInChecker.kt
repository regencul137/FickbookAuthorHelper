package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

interface IHttpSignInChecker {
    suspend fun isSignedIn(): Result<Boolean>
}

class HttpSignInChecker @Inject constructor(
    private val clientProvider: IClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpSignInChecker {
    sealed class Event : IEvent {
        data object CheckSignInStarted : Event()
        data class SignInChecked(val isSignedIn: Boolean) : Event()
    }

    override suspend fun isSignedIn(): Result<Boolean> {
        eventEmitter.pushEvent(Event.CheckSignInStarted)

        val userBlockId = "profile-holder"

        val request = Request.Builder()
            .url(FICBOOK_URL)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.success(false)
                    val isSignedIn = body.contains(userBlockId)
                    eventEmitter.pushEvent(Event.SignInChecked(isSignedIn))
                    Result.success(isSignedIn)
                } else {
                    eventEmitter.pushEvent(Event.SignInChecked(false))
                    Result.success(false)
                }
            } catch (e: IOException) {
                eventEmitter.pushEvent(Event.SignInChecked(false))
                Result.failure(e)
            }
        }
    }
}