package com.example.fickbookauthorhelper.logic

import com.example.fickbookauthorhelper.logic.http.IHttpConnectionChecker
import com.example.fickbookauthorhelper.logic.http.IHttpProxyFetcher
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsProvider
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsSaver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Singleton

interface IProxyManager {
    val proxyFromSettings: ProxyServer?
    suspend fun checkProxyInSettings(client: OkHttpClient, timeout: Long): ProxyServer?
    suspend fun findAndSaveWorkingProxy(client: OkHttpClient, timeout: Long): ProxyServer?
}

data class ProxyServer(val ip: String, val port: Int, val country: String)

@Singleton
class ProxyManager @Inject constructor(
    private val proxyFetcher: IHttpProxyFetcher,
    private val connectionChecker: IHttpConnectionChecker,
    private val appSettingsProvider: IAppSettingsProvider,
    private val appSettingsSaver: IAppSettingsSaver,
    private val eventEmitter: IEventEmitter
) : IProxyManager {
    sealed class Event : IEvent {
        data object ProxyServersAreLoading : Event()
        data class ProxyServersLoaded(val servers: List<ProxyServer>) : Event()
        data class ProxyServerUpdated(val proxyServer: ProxyServer) : Event()
        data class ProxyIsChecking(val proxyServer: ProxyServer) : Event()
    }

    override val proxyFromSettings: ProxyServer?
        get() = appSettingsProvider.getProxy()

    override suspend fun checkProxyInSettings(client: OkHttpClient, timeout: Long): ProxyServer? {
        proxyFromSettings?.let { proxy ->
            val configuredClient = client.newBuilder()
                .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxy.ip, proxy.port)))
                .build()

            connectionChecker.checkConnection(configuredClient, timeout).fold(
                onFailure = {
                    appSettingsSaver.clearProxy()
                    return null
                },
                onSuccess = {
                    return if (it) {
                        proxy
                    } else {
                        appSettingsSaver.clearProxy()
                        null
                    }
                })
        } ?: return null
    }

    private val findAndSaveWorkingProxyMutex = Mutex()

    override suspend fun findAndSaveWorkingProxy(client: OkHttpClient, timeout: Long): ProxyServer? {
        return findAndSaveWorkingProxyMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    println("findAndSaveWorkingProxy start")
                    eventEmitter.pushEvent(Event.ProxyServersAreLoading)

                    val proxies = proxyFetcher.fetchProxies(client).getOrElse {
                        return@withContext null
                    }
                    println("findAndSaveWorkingProxy found ${proxies.size}")
                    eventEmitter.pushEvent(Event.ProxyServersLoaded(proxies))

                    for (proxy in proxies) {
                        eventEmitter.pushEvent(Event.ProxyIsChecking(proxy))

                        val configuredClient = client.newBuilder()
                            .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxy.ip, proxy.port)))
                            .build()

                        val isWorking = connectionChecker.checkConnection(configuredClient, timeout).getOrElse { false }
                        if (isWorking) {
                            println("findAndSaveWorkingProxy success ${proxy.ip}:${proxy.port}")
                            appSettingsSaver.saveProxy(proxy)
                            eventEmitter.pushEvent(Event.ProxyServerUpdated(proxy))
                            return@withContext proxy
                        }
                    }

                    println("findAndSaveWorkingProxy: No working proxy found")
                    null

                } catch (e: Exception) {
                    println("findAndSaveWorkingProxy error: ${e.message}")
                    null
                }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ProxyManagerModule {
    @Singleton
    @Provides
    fun provideProxyManager(
        proxyFetcher: IHttpProxyFetcher,
        connectionChecker: IHttpConnectionChecker,
        appSettingsProvider: IAppSettingsProvider,
        appSettingsSaver: IAppSettingsSaver,
        eventEmitter: IEventEmitter
    ): ProxyManager {
        return ProxyManager(proxyFetcher, connectionChecker, appSettingsProvider, appSettingsSaver, eventEmitter)
    }

    @Singleton
    @Provides
    fun provideIProxyManager(proxyManager: ProxyManager): IProxyManager = proxyManager
}
