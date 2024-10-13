package com.example.fickbookauthorhelper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.feed.FeedManager
import com.example.fickbookauthorhelper.logic.http.IHttpFeedLoader
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.views.GainView

@Composable
fun FeedView(model: FeedViewModel = viewModel()) {
    val feedBlocks by model.feedBlocks.observeAsState(initial = emptyList())
    val isLoading by model.isLoading.observeAsState(initial = false)

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading && feedBlocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (feedBlocks.isNotEmpty()) {
                FeedColumn(feedItemModels = feedBlocks)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_feed).uppercase(),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedColumn(
    modifier: Modifier = Modifier,
    feedItemModels: List<FeedViewModel.Block>
) {
    var maxTitleWidth by remember { mutableIntStateOf(0) }
    feedItemModels.forEach { itemModel -> // Calculate width
        Box(
            modifier = Modifier
                .height(0.dp)
                .onGloballyPositioned { coordinates ->
                    val width = coordinates.size.width
                    if (width > maxTitleWidth) {
                        maxTitleWidth = width
                    }
                }
        ) {
            TitleText(
                title = stringResource(id = itemModel.titleId),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    Column(modifier = modifier) {
        feedItemModels.forEach { block ->
            FeedItem(
                itemModel = block,
                titleWidth = maxTitleWidth,
                isFirst = feedItemModels.indexOf(block) == 0,
                isLast = feedItemModels.indexOf(block) == feedItemModels.size - 1
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeedItem(
    itemModel: FeedViewModel.Block,
    titleWidth: Int,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val titleHeight = 42.dp
    val titleShapeDp = 18.dp
    val innerPaddingHorizontal = 12.dp
    val itemShapeDp = 18.dp
    val itemSmallShapeDp = 6.dp

    val itemShape = RoundedCornerShape(
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(if (isFirst) 0.dp else itemSmallShapeDp),
        bottomEnd = CornerSize(if (isLast) itemShapeDp else itemSmallShapeDp),
        bottomStart = CornerSize(itemSmallShapeDp),
    )
    val titleShape = RoundedCornerShape(
        bottomEnd = CornerSize(titleShapeDp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(0.dp)
    )
    val titlePaddingStart = 8
    val titlePaddingEnd = 16
    val titleWidthWithPadding = titleWidth + titlePaddingStart + titlePaddingEnd + 150
    val titleBlockWidthDp = with(LocalDensity.current) { titleWidthWithPadding.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = innerPaddingHorizontal)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(titleHeight)
                .zIndex(1f)
                .width(titleBlockWidthDp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = titleShape
                )
                .padding(vertical = 6.dp)
                .padding(start = titlePaddingStart.dp, end = titlePaddingEnd.dp)
        ) {
            Title(
                title = stringResource(id = itemModel.titleId),
                gain = itemModel.data.sumOf { it.gain },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                width = titleWidthWithPadding
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = innerPaddingHorizontal)
                .background(
                    color = MaterialTheme.colorScheme.onSecondary,
                    shape = itemShape
                )
                .padding(start = innerPaddingHorizontal)
                .padding(horizontal = 6.dp)
                .padding(top = 8.dp, bottom = 12.dp)
                .padding(top = titleHeight)
        ) {
            itemModel.data.forEach { ficStat ->
                Row {
                    Text(
                        text = "${ficStat.ficName}: ${ficStat.stat}",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Light
                        )
                    )
                    androidx.compose.animation.AnimatedVisibility(
                        modifier = Modifier
                            .offset(y = (-5).dp)
                            .padding(start = 5.dp),
                        visible = ficStat.gain > 0, label = ""
                    ) {
                        Text(
                            text = "+" + ficStat.gain,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W800
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(
    title: String,
    gain: Int,
    width: Int,
    color: Color
) {
    Row(
        modifier = Modifier.width(width.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleText(title = title, color = color)
        Spacer(modifier = Modifier.width(16.dp))
        AnimatedVisibility(
            visible = gain > 0,
            modifier = Modifier.offset(y = (-12).dp)
        ) {
            GainView(
                gain = gain,
                backgroundColor = Color.White,
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TitleText(title: String, color: Color) {
    Text(
        text = title,
        style = TextStyle(
            color = color,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp
        )
    )
}

//region Preview

@Preview
@Composable
private fun FeedColumnPreview() {
    FickbookAuthorHelperTheme {
        FeedColumn(feedItemModels = sample, modifier = Modifier.padding(25.dp))
    }
}

private val sample: List<FeedViewModel.Block> = listOf(
    FeedViewModel.Block(
        titleId = R.string.views,
        data = listOf(FeedManager.GainFeed.GainFicStat(12, IHttpFeedLoader.FicStat("Fanfic 1", 100)))
    ),
    FeedViewModel.Block(
        titleId = R.string.kudos,
        data = listOf(FeedManager.GainFeed.GainFicStat(0, IHttpFeedLoader.FicStat("Fanfic 3", 22)))
    ),
    FeedViewModel.Block(
        titleId = R.string.reviews,
        data = listOf(FeedManager.GainFeed.GainFicStat(3, IHttpFeedLoader.FicStat("Fanfic 2", 8)))
    )
)
//endregion



