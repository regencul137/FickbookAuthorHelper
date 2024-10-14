package com.example.fickbookauthorhelper.ui.initialization

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.managers.AuthManager
import com.example.fickbookauthorhelper.logic.managers.IAuthManager
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.http.HttpConnectionChecker
import com.example.fickbookauthorhelper.logic.http.HttpSignInChecker
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsProvider
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitializationViewModel @Inject constructor(
    private val authManager: IAuthManager,
    private val appSettingsSaver: IAppSettingsSaver,
    private val appSettingsProvider: IAppSettingsProvider,
    private val eventProvider: IEventProvider
) : ViewModel() {
    sealed class State {
        data class InProgress(@StringRes val descriptionId: Int) : State()
        data class SignInWithSavedCredentials(val userName: String) : State()
        data object ConnectionFailed : State()
        data object Success : State()
    }

    private val _state = MutableStateFlow<State>(State.InProgress(R.string.please_wait))
    val state: StateFlow<State> = _state.asStateFlow()

    private var _isVPNEnabled: MutableLiveData<Boolean> = MutableLiveData(appSettingsProvider.isVpnEnabled())
    val isVPNEnabled: LiveData<Boolean> = _isVPNEnabled

    init {
        viewModelScope.launch {
            eventProvider.events.collectLatest {
                when (it) {
                    is HttpConnectionChecker.Event.ConnectionCheckingStart -> {
                        pushState(State.InProgress(R.string.connection_checking))
                    }

                    is HttpConnectionChecker.Event.ConnectionChecked -> {
                        if (!it.isSuccess) {
                            pushState(State.ConnectionFailed)
                        }
                    }

                    is HttpSignInChecker.Event.CheckSignInStarted -> {
                        pushState(State.InProgress(R.string.sign_in_checking))
                    }

                    is AuthManager.Event.CredentialsLoaded -> {
                        pushState(State.SignInWithSavedCredentials(it.username))
                    }
                }
            }
        }
    }

    fun checkConnectionAgain() {
        authManager.startInitialization()
    }

    fun enableVpn() {
        appSettingsSaver.setVpnEnabled(true)
        _isVPNEnabled.value = true
    }

    private fun pushState(newState: State) {
        viewModelScope.launch { _state.emit(newState) }
    }
}