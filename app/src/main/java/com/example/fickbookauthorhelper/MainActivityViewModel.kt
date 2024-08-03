package com.example.fickbookauthorhelper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.ui.FeedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val authManager: IAuthManager) : ViewModel() {
    sealed class State {
        data object NotSignedIn : State()
        data object SigningIn : State()
        class SignedIn(val user: FHAuthManager.User, val feed: FeedViewModel) : State()
    }

    private val _state = MutableStateFlow<State>(State.NotSignedIn)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        if (authManager.isSignedIn) {
            authManager.currentUser?.let {
                viewModelScope.launch { _state.emit(State.SignedIn(it, FeedViewModel())) }
            }
        }
    }

    fun onSignInClick() {
        viewModelScope.launch { _state.emit(State.SigningIn) }
    }
}