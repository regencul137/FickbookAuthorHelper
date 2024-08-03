package com.example.fickbookauthorhelper.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@Composable
fun Feed(model: FeedViewModel) {

}

class FeedViewModel {
    val items: List<FeedItemViewModel>
        get() = listOf()
}

class FeedItemViewModel(title: String, count: Int, date: Long) {
    private val _title = MutableLiveData(title)
    private val title: LiveData<String>
        get() = _title
    private val _count = MutableLiveData(count)
    private val count: LiveData<Int>
        get() = _count
    private val _date = MutableLiveData(date)
    private val date: LiveData<Long>
        get() = _date
}

