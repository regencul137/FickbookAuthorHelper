package com.example.fickbookauthorhelper.ui.feed

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.IEventProvider
import com.example.fickbookauthorhelper.logic.feed.FeedManager
import com.example.fickbookauthorhelper.logic.feed.FeedType
import com.example.fickbookauthorhelper.logic.feed.IFeedManager
import com.example.fickbookauthorhelper.logic.feed.IFeedProvider
import com.example.fickbookauthorhelper.logic.http.HttpFeedLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedManager: IFeedManager,
    private val feedProvider: IFeedProvider,
    private val eventProvider: IEventProvider
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _feedBlocks = MutableLiveData<List<Block>>()
    val feedBlocks: LiveData<List<Block>> get() = _feedBlocks

    private val _lastUpdate = MutableLiveData<Long?>()
    val lastUpdate: LiveData<Long?> get() = _lastUpdate

    init {
        viewModelScope.launch {
            eventProvider.events.collectLatest {
                when (it) {
                    HttpFeedLoader.Event.FeedLoadingStarted -> _isLoading.value = true
                    is HttpFeedLoader.Event.FeedLoadingEnded -> _isLoading.value = false
                }
            }
        }
        collectData()
    }

    fun markAllAsRead() {
        feedManager.saveFeedToStorage()
        viewModelScope.launch { feedManager.updateFeedFromSite() }
    }

    private fun collectData() {
        viewModelScope.launch {
            feedProvider.feedFlow.collect { feed ->
                feed?.let {
                    val blocks = arrayListOf<Block>()
                    feed.stats.forEach { (feedType, data) ->
                        data?.let {
                            blocks.add(Block(titleId = titleIdForFeedType(feedType), data = it))
                        }
                    }

                    _feedBlocks.value = blocks
                }
            }
        }
        viewModelScope.launch {
            feedProvider.lastUpdateTimeFlow.collect { lastUpdateTime ->
                _lastUpdate.value = lastUpdateTime
            }
        }
    }

    @StringRes
    private fun titleIdForFeedType(feedType: FeedType): Int {
        return when (feedType) {
            FeedType.VIEWS -> R.string.views
            FeedType.KUDOS -> R.string.kudos
            FeedType.SUBSCRIPTIONS -> R.string.subscriptions
            FeedType.WAITS -> R.string.waits
            FeedType.REVIEWS -> R.string.reviews
        }
    }

    data class Block(
        @StringRes val titleId: Int,
        val data: List<FeedManager.GainFeed.GainFicStat>
    )
}
