package com.example.fickbookauthorhelper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.views.getTimeAgo
import kotlinx.coroutines.delay

@Composable
fun SettingsDialog(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    model: SettingsDialogViewModel = viewModel()
) {
    val isVpnEnabled by model.isVpnEnabled.observeAsState(initial = false)
    val lastUpdateTime by model.lastUpdate.observeAsState(initial = null)

    SettingsDialog(
        isExpanded = isExpanded,
        onDismiss = onDismiss,
        isVPNEnabled = isVpnEnabled,
        lastUpdateTime = lastUpdateTime,
        onVpnClick = { model.toggleVpn() },
        onSignOutClick = { model.signOut() })
}

@Composable
private fun SettingsDialog(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    isVPNEnabled: Boolean,
    lastUpdateTime: Long?,
    onVpnClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp),
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = (-0).dp, y = 0.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        VpnMenuMenuItem(isVPNEnabled) { onVpnClick() }
        SignOutMenuItem { onSignOutClick() }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(top = 8.dp)
        )
        LastUpdateMenuItem(
            lastUpdateTime, modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(end = 8.dp)
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun VpnMenuMenuItem(isVPNEnabled: Boolean, onVpnClick: () -> Unit) {
    DropdownMenuItem(
        modifier = Modifier.width(260.dp),
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuItemText(stringResource(R.string.use_vpn))
                Switch(checked = isVPNEnabled, onCheckedChange = { onVpnClick() })
            }
        },
        onClick = { onVpnClick() })
}

@Composable
private fun SignOutMenuItem(onSignOutClick: () -> Unit) {
    DropdownMenuItem(
        modifier = Modifier.fillMaxWidth(),
        text = {
            MenuItemText(stringResource(R.string.sign_out))
        },
        onClick = { onSignOutClick() })
}

@Composable
private fun LastUpdateMenuItem(lastUpdateTime: Long?, modifier: Modifier) {
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

    Text(
        modifier = modifier,
        text = stringResource(R.string.last_update, updateTimeText),
        style = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraLight
        )
    )
}

@Composable
private fun MenuItemText(text: String) {
    Text(
        text = text,
        style = TextStyle(fontSize = 17.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    )
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    FickbookAuthorHelperTheme {
        SettingsDialog(
            isExpanded = true,
            lastUpdateTime = null,
            isVPNEnabled = true,
            onVpnClick = {},
            onSignOutClick = {},
            onDismiss = {})
    }
}