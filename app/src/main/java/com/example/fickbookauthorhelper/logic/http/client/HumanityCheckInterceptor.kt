package com.example.fickbookauthorhelper.logic.http.client

import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.IEventProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumanityCheckInterceptor @Inject constructor(
    private val eventEmitter: IEventEmitter,
    private val eventProvider: IEventProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBody = response.body?.string()
        if (responseBody?.contains("Just a moment") == true || responseBody?.contains("cf_chl_captcha_tk") == true) {
            eventEmitter.pushEvent(ClientProvider.Event.HumanityCheck)

            val humanityCheckDone = CompletableDeferred<Unit>()
            runBlocking {
                eventProvider.events.collectLatest { event ->
                    if (event == ClientProvider.Event.HumanityCheckDone) {
                        humanityCheckDone.complete(Unit)
                    }
                }
                humanityCheckDone.await()
            }
        }

        return response.newBuilder()
            .body(responseBody?.toResponseBody(response.body?.contentType()))
            .build()
    }
}
