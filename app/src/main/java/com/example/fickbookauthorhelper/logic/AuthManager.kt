package com.example.fickbookauthorhelper.logic

import com.example.fickbookauthorhelper.logic.http.IHttpFeedLoader
import com.example.fickbookauthorhelper.logic.http.IHttpSignInChecker
import com.example.fickbookauthorhelper.logic.http.IHttpSignInHelper
import com.example.fickbookauthorhelper.logic.http.IHttpSignOutHelper
import com.example.fickbookauthorhelper.logic.storage.ISecureStorageProvider
import com.example.fickbookauthorhelper.logic.storage.ISecureStorageSaver
import com.example.fickbookauthorhelper.logic.storage.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface IAuthManager {
    val isSignedIn: StateFlow<Boolean?>
    val savedUsername: String?
    fun startInitialization()
    suspend fun signIn(username: String, password: String, rememberMe: Boolean): Result<Unit>
    suspend fun signOut(): Result<Boolean>
}

class AuthManager @Inject constructor(
    private val eventProvider: IEventProvider,
    private val eventEmitter: IEventEmitter,
    private val httpCheckSignInHelper: IHttpSignInChecker,
    private val httpSignInHelper: IHttpSignInHelper,
    private val httpSignOutHelper: IHttpSignOutHelper,
    private val httpFeedLoader: IHttpFeedLoader,
    private val userManager: UserManager,
    private val secureStorageSaver: ISecureStorageSaver,
    private val secureStorageProvider: ISecureStorageProvider
) : IAuthManager {
    sealed class Event : IEvent {
        data class CredentialsLoaded(val username: String) : Event()
    }

    private val _isSignedIn: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val isSignedIn: StateFlow<Boolean?> = _isSignedIn.asStateFlow()

    override val savedUsername: String?
        get() = secureStorageProvider.getUsername()

    init {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch { handleEvents() }
        startInitialization()
    }

    override fun startInitialization() {
        CoroutineScope(Dispatchers.IO).launch {
            httpCheckSignInHelper.isSignedIn()
                .onSuccess {
                    if (it) {
                        _isSignedIn.emit(true)
                        userManager.loadUser().onFailure { _isSignedIn.emit(false) }
                        httpFeedLoader.loadFeed()
                    } else {
                        tryToSignInUsingSavedCredentials()
                    }
                }
                .onFailure {
                    tryToSignInUsingSavedCredentials()
                }
        }
    }

    override suspend fun signIn(username: String, password: String, rememberMe: Boolean): Result<Unit> {
        val result = httpSignInHelper.signIn(username, password)
        result.onSuccess {
            if (rememberMe) {
                secureStorageSaver.saveUsername(username)
                secureStorageSaver.savePassword(password)
            }
            _isSignedIn.emit(true)
            userManager.loadUser()
            httpFeedLoader.loadFeed()
        }
        return result
    }

    override suspend fun signOut(): Result<Boolean> {
        return httpSignOutHelper.signOut()
    }

    private suspend fun handleEvents() {
        eventProvider.events.collectLatest {
            when (it) {
                is UserManager.Event.UserNameLoaded -> {
                    secureStorageSaver.saveUsername(it.name)
                }
            }
        }
    }

    private suspend fun tryToSignInUsingSavedCredentials() {
        val savedUsername = secureStorageProvider.getUsername()
        val savedPassword = secureStorageProvider.getPassword()

        if (!savedUsername.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            eventEmitter.pushEvent(Event.CredentialsLoaded(savedUsername))
            signIn(savedUsername, savedPassword, true)
                .onFailure {
                    secureStorageSaver.savePassword("")
                }
        } else {
            _isSignedIn.emit(false)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthManager(
        eventProvider: IEventProvider,
        eventEmitter: IEventEmitter,
        checkSignInHelper: IHttpSignInChecker,
        signInHelper: IHttpSignInHelper,
        httpSignOutHelper: IHttpSignOutHelper,
        feedLoader: IHttpFeedLoader,
        userManager: UserManager,
        secureStorage: SecureStorage
    ): AuthManager {
        return AuthManager(
            eventProvider,
            eventEmitter,
            checkSignInHelper,
            signInHelper,
            httpSignOutHelper,
            feedLoader,
            userManager,
            secureStorage,
            secureStorage
        )
    }

    @Provides
    @Singleton
    fun provideIAuthManager(authManager: AuthManager): IAuthManager = authManager
}
