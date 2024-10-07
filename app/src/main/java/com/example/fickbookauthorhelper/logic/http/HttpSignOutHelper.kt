package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

interface IHttpSignOutHelper {
    suspend fun signOut(): Result<Boolean>
}

class HttpSignOutHelper @Inject constructor(
    private val clientProvider: IClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpSignOutHelper {
    sealed class Event : IEvent {
        data object SignedOutEvent : Event()
    }

    override suspend fun signOut(): Result<Boolean> {
        val loginPath = "$FICBOOK_URL/logout"
        val request = Request.Builder().url(loginPath).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        try {
                            val responseJson = JSONObject(it)
                            if (responseJson.optBoolean("result", false)) {
                                eventEmitter.pushEvent(Event.SignedOutEvent)
                                Result.success(true)
                            } else {
                                val errorJson = responseJson.getJSONObject("error")
                                println("HttpSignOutHelper$errorJson")
                                Result.success(false)
                            }
                        } catch (e: IOException) {
                            Result.failure(IHttpSignInHelper.SignInException.ParseException)
                        }
                    } ?: run {
                        Result.failure(IHttpSignInHelper.SignInException.UnknownSignInException)
                    }
                } else {
                    Result.failure(IHttpSignInHelper.SignInException.RequestException(response.code))
                }
            } catch (exception: SocketException) {
                Result.failure(IHttpSignInHelper.SignInException.ConnectionResetException)
            } catch (exception: IOException) {
                Result.failure(IHttpSignInHelper.SignInException.UnknownBackendException)
            }
        }
    }
}