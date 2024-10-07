package com.example.fickbookauthorhelper.logic

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor() : DefaultLifecycleObserver {
    private val _appInBackground = MutableStateFlow(false)
    val appInBackground: StateFlow<Boolean> get() = _appInBackground

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        _appInBackground.value = false
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        _appInBackground.value = false
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _appInBackground.value = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        _appInBackground.value = true
    }
}