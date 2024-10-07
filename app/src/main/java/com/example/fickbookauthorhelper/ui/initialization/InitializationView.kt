package com.example.fickbookauthorhelper.ui.initialization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.views.ConnectionFailedView
import com.example.fickbookauthorhelper.ui.views.LoadingView

@Composable
fun InitializationView(
    modifier: Modifier = Modifier,
    model: InitializationViewModel = viewModel()
) {
    val state by model.state.collectAsState()
    val isVPNEnabled by model.isVPNEnabled.observeAsState(initial = true)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val currentState = state) {
            InitializationViewModel.State.ConnectionFailed -> {
                ConnectionFailed(isVpnEnabled = isVPNEnabled,
                    onVpnClick = { model.enableVpn() },
                    onRetryClick = { model.checkConnectionAgain() })
            }

            is InitializationViewModel.State.InProgress -> {
                LoadingView(
                    title = stringResource(id = currentState.descriptionId),
                    modifier = Modifier.padding(48.dp)
                )
            }

            InitializationViewModel.State.Success -> {
                Text(
                    text = stringResource(R.string.already_logged_in),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            is InitializationViewModel.State.SignInWithSavedCredentials -> {
                LoadingView(
                    title = stringResource(id = R.string.sign_in_with_saved_credentials, currentState.userName),
                    modifier = Modifier.padding(48.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectionFailed(
    modifier: Modifier = Modifier,
    isVpnEnabled: Boolean,
    onVpnClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ConnectionFailedView()
        Spacer(modifier = Modifier.height(12.dp))
        if (!isVpnEnabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Use VPN",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Switch(checked = false, onCheckedChange = { onVpnClick() })
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        OutlinedButton(onClick = onRetryClick) {
            Text(
                text = stringResource(R.string.retry),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 21.sp,
                    letterSpacing = 3.sp
                )
            )
        }
    }
}

//region Preview
@Preview(showBackground = true)
@Composable
private fun ConnectionFailedPreview() {
    FickbookAuthorHelperTheme {
        ConnectionFailed(isVpnEnabled = false, onVpnClick = { }, onRetryClick = {})
    }
}
//endregion