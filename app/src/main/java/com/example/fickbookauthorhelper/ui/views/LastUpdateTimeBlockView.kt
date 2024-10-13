package com.example.fickbookauthorhelper.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun LastUpdateTimeBlockView(
    lastUpdateTime: Long?,
    isLoading: Boolean,
    hasNewFeed: Boolean,
    onReadClick: () -> Unit
) {
    val neverText = stringResource(R.string.never)
    var updateTimeText by remember { mutableStateOf("") }

    LaunchedEffect(lastUpdateTime) {
        while (true) {
            updateTimeText = lastUpdateTime?.let {
                getTimeAgo(it)
            } ?: neverText
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.last_update, updateTimeText),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraLight
                )
            )
            AnimatedVisibility(visible = !isLoading && lastUpdateTime != null && hasNewFeed) {
                OutlinedButton(onClick = { onReadClick() }) {
                    Text(text = stringResource(R.string.mark_all_as_read))
                }
            }
        }
        Column {
            if (isLoading) {
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            } else {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    thickness = 3.dp
                )
            }
        }
    }
}

fun getTimeAgo(time: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "$seconds seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        else -> "$days days ago"
    }
}

@Preview
@Composable
private fun LastUpdateTimeBlockPreview() {
    FickbookAuthorHelperTheme {
        LastUpdateTimeBlockView(lastUpdateTime = 1724494267906,
            isLoading = false,
            hasNewFeed = true,
            onReadClick = {})
    }
}

@Preview
@Composable
private fun LastUpdateTimeBlockLoadingPreview() {
    FickbookAuthorHelperTheme {
        LastUpdateTimeBlockView(lastUpdateTime = 1724494267906,
            isLoading = true,
            hasNewFeed = true,
            onReadClick = {})
    }
}