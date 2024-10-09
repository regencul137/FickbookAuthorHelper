package com.example.fickbookauthorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fickbookauthorhelper.logic.AppLifecycleObserver
import com.example.fickbookauthorhelper.ui.feed.FeedView
import com.example.fickbookauthorhelper.ui.initialization.InitializationView
import com.example.fickbookauthorhelper.ui.signIn.SignIn
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.user.UserView
import com.example.fickbookauthorhelper.ui.views.RequestPermissionsView
import com.example.fickbookauthorhelper.ui.views.WebView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    @Inject
    lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                || state is MainViewModel.State.PermissionNeeded
            ) Arrangement.Center else Arrangement.Top,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val currentState = state) {
                MainViewModel.State.HumanityCheck -> {
                    WebView(
                        url = "https://ficbook-proxer.iameverybody.workers.dev/?url=https://ficbook.net",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                MainViewModel.State.Initialization -> {
                    InitializationView(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is MainViewModel.State.SignedIn -> {
                    FeedView()
                }

                MainViewModel.State.SigningIn -> {
                    SignIn(
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
            }
        }
    }
}

