package com.example.fickbookauthorhelper

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.logic.AuthManager
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.PermissionHelper
import com.example.fickbookauthorhelper.logic.feed.IFeedManager
import com.example.fickbookauthorhelper.logic.feed.IFeedProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionHelper: PermissionHelper,
    private val feedProvider: IFeedProvider,
    private val feedManager: IFeedManager,
    private val eventProvider: IEventProvider
) : ViewModel() {
    sealed class State {
        data class PermissionNeeded(val permissions: List<String>) : State()

        //data object HumanityCheck : State()
        data object Initialization : State()
        data object SigningIn : State()
        data object SignedIn : State()
        data object SigningOut : State()
    }

    private val _state = MutableStateFlow<State>(State.Initialization)
    val state: StateFlow<State> = _state.asStateFlow()
    private var previousState: State = State.Initialization

    private var _gainFeed = MutableLiveData(0)
    val gainFeed: LiveData<Int>
        get() = _gainFeed

    init {
        viewModelScope.launch {
            eventProvider.events.collectLatest {
                when (it) {
                    /*is ClientProvider.Event.HumanityCheck -> {
                        _state.emit(State.HumanityCheck)
                    }*/

                    is AuthManager.Event.SignedIn -> {
                        _state.emit(State.SignedIn)
                    }

                    is AuthManager.Event.SignedOut -> {
                        _state.emit(State.SigningIn)
                    }

                    is AuthManager.Event.SigningOut -> {
                        _state.emit(State.SigningOut)
                    }
                }
            }
        }
        viewModelScope.launch {
            feedProvider.feedFlow.collectLatest {
                _gainFeed.value = it?.gainCount ?: 0
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

    fun markFeedAsRead() {
        viewModelScope.launch {
            feedManager.saveFeedToStorage()
            feedManager.updateFeedFromSite()
        }
    }
}