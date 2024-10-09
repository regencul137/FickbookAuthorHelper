package com.example.fickbookauthorhelper.logic

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor() : DefaultLifecycleObserver {
    private val _appInBackgroundFlow = MutableStateFlow(false)
    val appInBackgroundFlow: StateFlow<Boolean> get() = _appInBackgroundFlow
    val appInBackground: Boolean
        get() = _appInBackgroundFlow.value

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        _appInBackgroundFlow.value = false
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        _appInBackgroundFlow.value = false
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _appInBackgroundFlow.value = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        _appInBackgroundFlow.value = true
    }
}