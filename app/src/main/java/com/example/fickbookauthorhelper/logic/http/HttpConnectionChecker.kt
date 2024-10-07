package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Duration
import javax.inject.Inject

interface IHttpConnectionChecker {
    suspend fun checkConnection(client: OkHttpClient, timeout: Long): Result<Boolean>
}

class HttpConnectionChecker @Inject constructor(private val eventEmitter: IEventEmitter) : IHttpConnectionChecker {
    sealed class Event : IEvent {
        data object ConnectionCheckingStart : Event()
        data class ConnectionChecked(val isSuccess: Boolean) : Event()
    }

    override suspend fun checkConnection(client: OkHttpClient, timeout: Long): Result<Boolean> {
        return checkReach(client, timeout)
            .onSuccess { eventEmitter.pushEvent(Event.ConnectionChecked(it)) }
            .onFailure { eventEmitter.pushEvent(Event.ConnectionChecked(false)) }
    }

    private suspend fun checkReach(client: OkHttpClient, timeout: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(Duration.ofMillis(timeout)) {
                    val deferred = CompletableDeferred<Response>()
                    val request = Request.Builder()
                        .url(FICBOOK_URL)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            deferred.completeExceptionally(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            deferred.complete(response)
                            val responseData = response.body?.string()
                            print(responseData)
                        }
                    })

                    val response = deferred.await()
                    Result.success(response.isSuccessful)
                }
            } catch (e: SocketTimeoutException) {
                Result.failure(e)
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: TimeoutCancellationException) {
                Result.failure(Exception("Job timed out"))
            }
        }
    }
}
