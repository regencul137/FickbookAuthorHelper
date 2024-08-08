package com.example.fickbookauthorhelper.logic.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

interface IHttpClientProvider {
    val client: OkHttpClient
}

class HttpClient @Inject constructor() : IHttpClientProvider {
    override val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            private val cookieStore = HashMap<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: ArrayList()
            }
        }).build()
}

@Module
@InstallIn(SingletonComponent::class)
object FHttpClientModule {
    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient()
    }

    @Singleton
    @Provides
    fun provideIHttpClientProvider(httpClient: HttpClient): IHttpClientProvider = httpClient
}