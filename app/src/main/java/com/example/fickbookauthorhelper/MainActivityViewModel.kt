package com.example.fickbookauthorhelper

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.logic.IAuthManager
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.ISignedInProvider
import com.example.fickbookauthorhelper.logic.PermissionHelper
import com.example.fickbookauthorhelper.logic.http.client.ClientProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signedInProvider: ISignedInProvider,
    private val permissionHelper: PermissionHelper,
    private val eventProvider: IEventProvider
) : ViewModel() {
    sealed class State {
        data class PermissionNeeded(val permissions: List<String>) : State()
        data object HumanityCheck : State()
        data object Initialization : State()
        data object SigningIn : State()
        data object SignedIn : State()
    }

    private val _state = MutableStateFlow<State>(State.Initialization)
    val state: StateFlow<State> = _state.asStateFlow()
    private var previousState: State = State.Initialization

    init {
        viewModelScope.launch {
            signedInProvider.isSignedIn.collectLatest { isSignedIn ->
                when (isSignedIn) {
                    true -> _state.emit(State.SignedIn)
                    false -> _state.emit(State.SigningIn)
                    null -> {}
                }
            }
        }
        viewModelScope.launch {
            eventProvider.events.collectLatest {
                if (it is ClientProvider.Event.HumanityCheck) {
                    _state.emit(State.HumanityCheck)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            observePermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observePermissions() {
        viewModelScope.launch {
            permissionHelper.absentPermissions.collectLatest { permissions ->
                if (permissions.isNotEmpty()) {
                    previousState = _state.value
                    _state.value = State.PermissionNeeded(permissions)
                } else {
                    if (_state.value is State.PermissionNeeded) {
                        _state.value = previousState
                    }
                }
            }
        }
    }

    fun onPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.checkPermissions()
        }
    }
}