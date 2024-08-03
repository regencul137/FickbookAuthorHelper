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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.ui.Feed
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import dagger.hilt.android.AndroidEntryPoint
import mockPageModel
import mockUser

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FickbookAuthorHelperTheme {
                Content(model)
            }
        }
    }
}

@Composable
private fun Content(model: MainViewModel) {
    val state by model.state.collectAsState()
    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            when (val currentState = state) {
                is MainViewModel.State.NotSignedIn,
                MainViewModel.State.SigningIn -> {
                }

                is MainViewModel.State.SignedIn -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                top = WindowInsets.systemBars
                                    .asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        UserBlock(user = currentState.user)
                    }
                }
            }
        }) { innerPadding ->
        Column(
            verticalArrangement = if (state is MainViewModel.State.SignedIn) Arrangement.Top else Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            when (val currentState = state) {
                MainViewModel.State.NotSignedIn -> {
                    SignedOutWarning(
                        onClick = { model.onSignInClick() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                is MainViewModel.State.SignedIn -> {
                    Feed(model = currentState.feed)
                }

                MainViewModel.State.SigningIn -> {
                    SignIn(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedOutWarning(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.warning_signed_out),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                disabledContainerColor = MaterialTheme.colorScheme.error,
                disabledContentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text(
                text = stringResource(R.string.sign_in).uppercase(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun SignIn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(18.dp)
    ) {
        OutlinedTextField(value = "", onValueChange = {})
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "", onValueChange = {})
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledContentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(
                text = stringResource(R.string.sign_in).uppercase(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun UserBlock(
    user: FHAuthManager.User,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = user.avatarId),
            contentDescription = "avatar",
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = user.name,
            maxLines = 1,
            style = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 21.sp
            )
        )
    }
}

@Preview
@Composable
private fun ContentPreview() {
    FickbookAuthorHelperTheme {
        Content(mockPageModel)
    }
}

@Preview
@Composable
private fun UserBlockPreview() {
    FickbookAuthorHelperTheme {
        UserBlock(
            user = mockUser
        )
    }
}

@Preview
@Composable
private fun SignedOutWarningPreview() {
    SignedOutWarning(onClick = {})
}

@Preview
@Composable
private fun SignInPreview() {
    SignIn(onClick = { })
}

