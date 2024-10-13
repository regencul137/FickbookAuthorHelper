package com.example.fickbookauthorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.logic.AppLifecycleObserver
import com.example.fickbookauthorhelper.logic.NotificationService
import com.example.fickbookauthorhelper.ui.feed.FeedView
import com.example.fickbookauthorhelper.ui.initialization.InitializationView
import com.example.fickbookauthorhelper.ui.signIn.SignInView
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.user.UserView
import com.example.fickbookauthorhelper.ui.views.GainView
import com.example.fickbookauthorhelper.ui.views.LoadingView
import com.example.fickbookauthorhelper.ui.views.RequestPermissionsView
import com.example.fickbookauthorhelper.ui.views.TitleView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    @Inject
    lateinit var lifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var notificationService: NotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationId = intent.getIntExtra(NotificationService.PARAM_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            notificationService.cancelNotification(notificationId)
        }

        lifecycle.addObserver(lifecycleObserver)
        enableEdgeToEdge()
        setContent {
            FickbookAuthorHelperTheme {
                window.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.background), contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Content(model)
                }
            }
        }
    }
}

@Composable
private fun Content(model: MainViewModel) {
    val state by model.state.collectAsState()
    val gainFeed by model.gainFeed.observeAsState(initial = 0)

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.Transparent),
        contentColor = Color.Transparent,
        containerColor = Color.Transparent,
        topBar = {
            if (state == MainViewModel.State.SignedIn)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            top = WindowInsets.systemBars
                                .asPaddingValues()
                                .calculateTopPadding()
                        )
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    UserView(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
        }) { innerPadding ->
        Column(
            verticalArrangement = if (state == MainViewModel.State.Initialization
                || state is MainViewModel.State.PermissionNeeded ||
                state is MainViewModel.State.SigningOut
            ) {
                Arrangement.Center
            } else {
                Arrangement.Top
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val currentState = state) {
                /*MainViewModel.State.HumanityCheck -> {
                    WebView(
                        url = "https://ficbook-proxer.iameverybody.workers.dev/?url=https://ficbook.net",
                        modifier = Modifier.fillMaxSize()
                    )
                }*/

                MainViewModel.State.Initialization -> {
                    InitializationView(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is MainViewModel.State.SignedIn -> {
                    Column {
                        var isExpanded by remember { mutableStateOf(true) }
                        TitleView(
                            modifier = Modifier.padding(bottom = 10.dp),
                            title = stringResource(R.string.feed),
                            isExpanded = isExpanded,
                            detailsComposable = {
                                AnimatedVisibility(visible = gainFeed > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            modifier = Modifier
                                                .clickable { model.markFeedAsRead() }
                                                .padding(5.dp),
                                            text = stringResource(R.string.mark_all_as_read),
                                            style = TextStyle(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        GainView(
                                            gain = gainFeed,
                                            backgroundColor = Color.White.copy(alpha = 0.4f),
                                            textColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        ) {
                            isExpanded = !isExpanded
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            FeedView()
                        }
                    }
                }

                MainViewModel.State.SigningIn -> {
                    SignInView(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 48.dp)
                            .padding(horizontal = 22.dp)
                    )
                }

                is MainViewModel.State.PermissionNeeded -> {
                    RequestPermissionsView(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        permissions = currentState.permissions,
                        onPermissionGranted = { model.onPermissionGranted() }
                    )
                }

                MainViewModel.State.SigningOut -> {
                    LoadingView(title = stringResource(R.string.logging_out))
                }
            }
        }
    }
}

