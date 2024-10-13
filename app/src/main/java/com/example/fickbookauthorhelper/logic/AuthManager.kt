package com.example.fickbookauthorhelper.logic

import android.content.Context
import androidx.work.WorkManager
import com.example.fickbookauthorhelper.logic.http.IHttpSignInChecker
import com.example.fickbookauthorhelper.logic.http.IHttpSignInHelper
import com.example.fickbookauthorhelper.logic.http.IHttpSignOutHelper
import com.example.fickbookauthorhelper.logic.storage.ISecureStorageProvider
import com.example.fickbookauthorhelper.logic.storage.ISecureStorageSaver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

interface ISignedInProvider {
    val isSignedIn: StateFlow<Boolean?>
}

interface IAuthManager {
    val savedUsername: String?
    fun startInitialization()
    suspend fun signIn(username: String, password: String, rememberMe: Boolean): Result<Unit>
    suspend fun signOut(): Result<Boolean>
}

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventProvider: IEventProvider,
    private val eventEmitter: IEventEmitter,
    private val httpCheckSignInHelper: IHttpSignInChecker,
    private val httpSignInHelper: IHttpSignInHelper,
    private val httpSignOutHelper: IHttpSignOutHelper,
    private val userManager: UserManager,
    private val secureStorageSaver: ISecureStorageSaver,
    private val secureStorageProvider: ISecureStorageProvider
) : IAuthManager, ISignedInProvider {
    sealed class Event : IEvent {
        data object SignedIn : Event()
        data object SigningOut : Event()
        data object SignedOut : Event()
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
                        handleSignedIn()
                        userManager.loadUser().onFailure { handleSignedOut() }
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
            handleSignedIn()
            userManager.loadUser().onFailure { handleSignedOut() }
        }
        return result
    }

    override suspend fun signOut(): Result<Boolean> {
        eventEmitter.pushEvent(Event.SigningOut)
        return httpSignOutHelper.signOut()
            .onSuccess {
                secureStorageSaver.savePassword("")
                handleSignedOut()
            }
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
            handleSignedOut()
        }
    }

    private fun handleSignedIn() {
        _isSignedIn.tryEmit(true)
        eventEmitter.pushEvent(Event.SignedIn)
    }

    private fun handleSignedOut() {
        _isSignedIn.tryEmit(false)
        eventEmitter.pushEvent(Event.SignedOut)
        WorkManager.getInstance(context).cancelAllWork()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthManagerModule {
    @Binds
    @Singleton
    abstract fun bindIAuthManager(authManager: AuthManager): IAuthManager

    @Binds
    @Singleton
    abstract fun bindISignedInProvider(authManager: AuthManager): ISignedInProvider
}
