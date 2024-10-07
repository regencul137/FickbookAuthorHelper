package com.example.fickbookauthorhelper.ui.user

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.logic.IUserManager
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsProvider
import com.example.fickbookauthorhelper.logic.storage.IAppSettingsSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userManager: IUserManager,
    private val appSettingsProvider: IAppSettingsProvider,
    private val appSettingsSaver: IAppSettingsSaver
) : ViewModel() {

    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> get() = _username

    private val _avatar = MutableLiveData<Drawable?>()
    val avatar: LiveData<Drawable?> get() = _avatar

    private val _isVpnEnabled = MutableLiveData<Boolean>()
    val isVpnEnabled: LiveData<Boolean> get() = _isVpnEnabled

    init {
        observeUserData()
        loadUserData()
        loadVpnSetting()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userManager.username.collect { name ->
                _username.value = name
            }
        }
        viewModelScope.launch {
            userManager.avatar.collect { drawable ->
                _avatar.value = drawable
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userManager.loadUser().onFailure {
                // Обработка ошибки, если необходимо
            }
        }
    }

    private fun loadVpnSetting() {
        _isVpnEnabled.value = appSettingsProvider.isVpnEnabled()
    }

    fun toggleVpn() {
        val currentStatus = _isVpnEnabled.value ?: false
        val newStatus = !currentStatus
        _isVpnEnabled.value = newStatus
        appSettingsSaver.setVpnEnabled(newStatus)
    }
}