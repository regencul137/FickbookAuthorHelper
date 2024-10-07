package com.example.fickbookauthorhelper.logic.http.client

import com.example.fickbookauthorhelper.logic.CookieManager
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.http.IHttpConnectionChecker
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

interface IClientProvider {
    suspend fun client(): OkHttpClient
    suspend fun updateClient()
}

@Singleton
class ClientProvider @Inject constructor(
    private val appSettingsProvider: IAppSettingsProvider,
    private val cookieManager: CookieManager,
    private val connectionChecker: IHttpConnectionChecker,
    private val eventProvider: IEventProvider,
    humanityInterceptor: HumanityCheckInterceptor
) : IClientProvider {
    companion object {
        const val TIMEOUT_MILLIS = 4000L
    }

    sealed class Event : IEvent {
        data object HumanityCheck : Event()
        data object HumanityCheckDone : Event()
    }

    private val clientBuilder = OkHttpClient.Builder()
        .followRedirects(true)
        .addInterceptor(humanityInterceptor)
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val cookieString = cookies.joinToString("\n") { it.toString() }
                println("{save cookies} $cookieString")
                cookieManager.saveCookies(cookieString)
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookieHeader = cookieManager.getCookies()
                val cookies = arrayListOf<Cookie>()
                cookieHeader?.split("\n")?.forEach {
                    Cookie.parse(url, it)?.let { cookie -> cookies.add(cookie) }
                }
                println("{load cookies} $cookies")
                return cookies
            }
        })

    private val cleanClient: OkHttpClient = createRegularHttpClient()
    private val cloudClient = CloudClient(clientBuilder)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            updateClient()
        }
    }

    private var client: OkHttpClient? = null
    override suspend fun client(): OkHttpClient {
        client?.let { return it } ?: run {
            val newClient = findClient()
            client = newClient
            return newClient
        }
    }

    override suspend fun updateClient() {
        client = findClient()
    }

    private fun findClient(): OkHttpClient {
        return if (appSettingsProvider.isVpnEnabled()) cloudClient else cleanClient;
    }

    private fun createRegularHttpClient(): OkHttpClient {
        return clientBuilder.build()
    }

    class CloudClient(private val builder: Builder) : OkHttpClient() {
        companion object {
            private const val WORKER_PROXY_URL = "https://proxer.iameverybody.workers.dev"
            private const val WORKER_SIGN_IN_URL = "https://sign-in.iameverybody.workers.dev"
        }

        override fun newCall(request: Request): Call {

            return newBuilder().build().newCall(generateCloudRequest(request))
        }

        override fun newBuilder(): Builder {
            return builder
        }

        private fun generateCloudRequest(request: Request): Request {
            val originalUrl = request.url.toString()

            val isSignInRequest = originalUrl.contains("/login_check", ignoreCase = true)

            val newUrl = if (isSignInRequest) {
                val signInParams = extractParamsFromRequestBody(request)
                val username = signInParams["login"]
                val password = signInParams["password"]

                if (username != null && password != null) {
                    "$WORKER_SIGN_IN_URL?username=${username}&password=${password}"
                } else {
                    "$WORKER_PROXY_URL?url=${originalUrl}"
                }
            } else {
                "$WORKER_PROXY_URL?url=${originalUrl}"
            }

            return request.newBuilder().url(newUrl).build()
        }

        private fun extractParamsFromRequestBody(request: Request): Map<String, String> {
            return try {
                val requestBody = request.body ?: return emptyMap()
                val buffer = okio.Buffer()
                requestBody.writeTo(buffer)
                val bodyString = buffer.readUtf8()

                bodyString.split("&")
                    .map { param -> param.split("=") }
                    .filter { it.size == 2 }
                    .associate { it[0] to it[1] }
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FHttpClientModule {
    @Singleton
    @Provides
    fun provideHttpClient(
        appSettingsProvider: IAppSettingsProvider,
        cookieManager: CookieManager,
        connectionChecker: IHttpConnectionChecker,
        eventProvider: IEventProvider,
        humanityInterceptor: HumanityCheckInterceptor
    ): ClientProvider {
        return ClientProvider(
            appSettingsProvider,
            cookieManager,
            connectionChecker,
            eventProvider,
            humanityInterceptor
        )
    }

    @Singleton
    @Provides
    fun provideIHttpClientProvider(httpClient: ClientProvider): IClientProvider = httpClient
}
