package com.example.fickbookauthorhelper.logic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class PermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lifecycleObserver: AppLifecycleObserver
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

    private val _absentPermissions = MutableStateFlow<List<String>>(listOf())
    val absentPermissions: StateFlow<List<String>> get() = _absentPermissions

    init {
        checkPermissions()
        CoroutineScope(Dispatchers.Default).launch {
            lifecycleObserver.appInBackgroundFlow.collectLatest {
                if (!it) {
                    checkPermissions()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermissions() {
        val absentPermissionsList = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        _absentPermissions.update { absentPermissionsList }
    }
}