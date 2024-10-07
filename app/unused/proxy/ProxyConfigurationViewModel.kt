package com.example.fickbookauthorhelper.ui.proxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.ProxyManager
import com.example.fickbookauthorhelper.logic.ProxyServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProxyConfigurationViewModel @Inject constructor(private val eventProvider: IEventProvider) : ViewModel() {
    sealed class State {
        data object Loading : State()
        data class CheckingSavedProxy(val proxy: ProxyServer) : State()
        data object LoadingList : State()
        data class ListLoaded(val proxyList: List<ProxyServer>) : State()
        data class Checking(val proxy: ProxyServer, val remainingProxiesAmount: Int, val proxiesAmount: Int) : State()
        data class Success(val proxy: ProxyServer) : State()
        data object Error : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> get() = _state.asStateFlow()
    private fun pushState(state: State) {
        viewModelScope.launch { _state.emit(state) }
    }

    private var proxyList: ArrayList<ProxyServer> = arrayListOf()
    private var proxiesAmount = 0

    init {
        viewModelScope.launch {
            eventProvider.events.collectLatest {
                when (it) {
                    ProxyManager.Event.ProxyServersAreLoading -> {
                        pushState(State.LoadingList)
                    }

                    is ProxyManager.Event.ProxyServersLoaded -> {
                        proxiesAmount = it.servers.size
                        proxyList = ArrayList(it.servers)
                        pushState(State.ListLoaded(proxyList))
                    }

                    is ProxyManager.Event.ProxyIsChecking -> {
                        proxyList.remove(it.proxyServer)
                        pushState(State.Checking(it.proxyServer, proxyList.size, proxiesAmount))
                    }

                    is ProxyManager.Event.ProxyServerUpdated -> {
                        pushState(State.Success(it.proxyServer))
                    }
                }
            }
        }
    }
}
