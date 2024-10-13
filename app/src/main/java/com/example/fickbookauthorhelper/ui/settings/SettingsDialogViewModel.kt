package com.example.fickbookauthorhelper.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.logic.AuthManager
import com.example.fickbookauthorhelper.logic.feed.FeedManager
import com.example.fickbookauthorhelper.logic.feed.IFeedProvider
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsProvider
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsDialogViewModel @Inject constructor(
    appSettingsProvider: IAppSettingsProvider,
    feedProvider: IFeedProvider,
    private val authManager: AuthManager,
    private val appSettingsSaver: IAppSettingsSaver
) : ViewModel() {
    private val _isVpnEnabled = MutableLiveData(appSettingsProvider.isVpnEnabled())
    val isVpnEnabled: LiveData<Boolean> get() = _isVpnEnabled

    private val _lastUpdate = MutableLiveData<Long?>()
    val lastUpdate: LiveData<Long?> get() = _lastUpdate

    init {
        viewModelScope.launch {
            feedProvider.lastUpdateTimeFlow.collect { lastUpdateTime ->
                _lastUpdate.value = lastUpdateTime
            }
        }
    }

    fun toggleVpn() {
        val currentStatus = _isVpnEnabled.value ?: false
        val newStatus = !currentStatus
        _isVpnEnabled.value = newStatus
        appSettingsSaver.setVpnEnabled(newStatus)
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    /*fun markAllAsRead() {
        feedManager.saveFeedToStorage()
        viewModelScope.launch { feedManager.updateFeedFromSite() }
    }*/
}