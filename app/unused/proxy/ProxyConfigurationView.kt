package com.example.fickbookauthorhelper.ui.proxy

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.logic.ProxyServer
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.views.ConnectionFailedView
import com.example.fickbookauthorhelper.ui.views.LoadingView
import java.util.Locale

@Composable
fun ProxyConfigurationView(model: ProxyConfigurationViewModel = viewModel()) {
    val state by model.state.collectAsState()

    ProxyConfigurationView(state = state)
}

@Composable
private fun ProxyConfigurationView(state: ProxyConfigurationViewModel.State) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            ProxyConfigurationViewModel.State.Loading -> {
                LoadingView(
                    title = stringResource(id = R.string.loading),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            is ProxyConfigurationViewModel.State.CheckingSavedProxy -> {
                LoadingView(
                    title = stringResource(R.string.checking_latest_proxy),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CheckingProxyView(
                    proxy = state.proxy,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            ProxyConfigurationViewModel.State.LoadingList -> {
                LoadingView(
                    title = stringResource(R.string.fetching_proxy_servers),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            is ProxyConfigurationViewModel.State.ListLoaded -> {
                LoadingView(
                    title = stringResource(R.string.search_working_proxy),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            is ProxyConfigurationViewModel.State.Checking -> {
                Image(
                    painter = painterResource(id = R.drawable.search),
                    modifier = Modifier.height(200.dp),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProgressBar(
                    proxiesCount = state.proxiesAmount,
                    proxiesForCheckCount = state.remainingProxiesAmount
                )
                Spacer(modifier = Modifier.height(24.dp))
                CheckingProxyView(
                    proxy = state.proxy,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            ProxyConfigurationViewModel.State.Error -> {
                ConnectionFailedView()
            }

            is ProxyConfigurationViewModel.State.Success -> {
                SuccessView(proxy = state.proxy)
            }
        }
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    proxiesCount: Int,
    proxiesForCheckCount: Int
) {
    val progress = if (proxiesForCheckCount > 0) {
        1 - proxiesForCheckCount.toFloat() / proxiesCount
    } else {
        0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 22.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                .height(8.dp)
                .weight(1f),
        )
        Spacer(modifier = Modifier.width(22.dp))
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color = MaterialTheme.colorScheme.onBackground, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                fontSize = 12.sp,
                letterSpacing = 0.005.sp,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.background,
                text = String.format(Locale.UK, "%.2f", progress * 100f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuccessView(modifier: Modifier = Modifier, proxy: ProxyServer) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Green.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.proxy_found),
            style = TextStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.W400
            )
        )
        Text(
            text = "${proxy.ip}:${proxy.port}",
            style = TextStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 19.sp,
                fontWeight = FontWeight.W300
            )
        )
    }
}

@Composable
private fun CheckingProxyView(
    modifier: Modifier = Modifier,
    proxy: ProxyServer
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val animatedBackground = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        start = Offset(animatedOffsetX, 0f),
        end = Offset(x = animatedOffsetX + 300f, y = 0f)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = animatedBackground,
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.attempt_to_connect).uppercase(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "${proxy.ip}:${proxy.port}",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.W300
                )
            )
        }
    }
}

@Composable
private fun ProxyListView(
    modifier: Modifier = Modifier,
    proxyList: List<ProxyServer>
) {
    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Light
    )
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.onSecondary,
                shape = MaterialTheme.shapes.large
            )
            .padding(8.dp)
    ) {
        items(proxyList) { proxy ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${proxy.ip}:${proxy.port} ",
                    style = textStyle
                )
                Text(
                    text = "[${proxy.country}]",
                    style = textStyle
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProxyConfigurationPreview() {
    FickbookAuthorHelperTheme {
        val stateLoading = ProxyConfigurationViewModel.State.Loading
        val stateCheckingSavedProxy = ProxyConfigurationViewModel.State.CheckingSavedProxy(
            ProxyServer(ip = "192.168.1.1", port = 8080, country = "USA")
        )
        val stateChecking = ProxyConfigurationViewModel.State.Checking(
            ProxyServer(ip = "192.168.1.1", port = 8080, country = "USA"),
            137,
            322
        )
        val stateListLoaded = ProxyConfigurationViewModel.State.ListLoaded(mockProxyList)
        ProxyConfigurationView(state = stateChecking)
    }
}

private val mockProxyList = listOf(
    ProxyServer(ip = "192.168.1.1", port = 8080, country = "USA"),
    ProxyServer(ip = "172.16.0.1", port = 3128, country = "Germany"),
    ProxyServer(ip = "10.0.0.1", port = 8888, country = "Japan")
)