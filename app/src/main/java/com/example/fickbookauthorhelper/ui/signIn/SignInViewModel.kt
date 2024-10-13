package com.example.fickbookauthorhelper.ui.signIn

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.IAuthManager
import com.example.fickbookauthorhelper.logic.http.IHttpSignInHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(private val authManager: IAuthManager) : ViewModel() {
    sealed class State {
        data object Input : State()
        data object RequestInProcess : State()
        data class Error(@StringRes val messageId: Int) : State()
    }

    private val _state = MutableStateFlow<State>(State.Input)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _login = MutableLiveData<String>()
    val login: LiveData<String> = _login

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _rememberMe = MutableLiveData<Boolean>()
    val rememberMe: LiveData<Boolean> = _rememberMe

    init {
        authManager.savedUsername?.let { _login.value = it }
    }

    fun onDispose() {
        setState(State.Input)
        _password.value = ""
    }

    internal fun onLoginChange(newLogin: String) {
        _login.value = newLogin
    }

    internal fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    internal fun onRememberMeChange(remember: Boolean) {
        _rememberMe.value = remember
    }

    fun signIn() {
        setState(State.RequestInProcess)
        viewModelScope.launch {
            authManager.signIn(
                username = _login.value.orEmpty(),
                password = _password.value.orEmpty(),
                rememberMe = _rememberMe.value ?: false
            )
                .onFailure {
                    when (it) {
                        IHttpSignInHelper.SignInException.ConnectionResetException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_connection_reset)
                            )
                        }

                        IHttpSignInHelper.SignInException.InvalidCredentialsException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_invalid_credentials)
                            )
                        }

                        IHttpSignInHelper.SignInException.ParseException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_parse_response)
                            )
                        }

                        is IHttpSignInHelper.SignInException.RequestException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_request_failed)
                            )
                        }

                        IHttpSignInHelper.SignInException.TimeOutException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_timeout)
                            )
                        }

                        IHttpSignInHelper.SignInException.UnknownBackendException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_backend_unknown_exception)
                            )
                        }

                        IHttpSignInHelper.SignInException.UnknownSignInException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_sign_in_failed)
                            )
                        }

                        IHttpSignInHelper.SignInException.UserNotFoundException -> {
                            setState(
                                state = State.Error(messageId = R.string.error_user_not_found)
                            )
                        }
                    }
                }
        }
    }

    private fun setState(state: State) {
        viewModelScope.launch { _state.emit(state) }
    }
}