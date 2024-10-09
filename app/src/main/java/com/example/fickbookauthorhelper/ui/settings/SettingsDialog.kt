package com.example.fickbookauthorhelper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R

@Composable
fun SettingsDialog(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    model: SettingsDialogViewModel = viewModel()
) {
    val isVpnEnabled by model.isVpnEnabled.observeAsState(initial = false)

    SettingsDialog(
        isExpanded = isExpanded,
        onDismiss = onDismiss,
        isVPNEnabled = isVpnEnabled,
        onVpnClick = { model.toggleVpn() },
        onSignOutClick = { model.signOut() })
}

@Composable
private fun SettingsDialog(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    isVPNEnabled: Boolean,
    onVpnClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = (-0).dp, y = 0.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        DropdownMenuItem(
            modifier = Modifier.width(200.dp),
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
        DropdownMenuItem(
            modifier = Modifier.width(200.dp),
            text = {
                MenuItemText(stringResource(R.string.sign_out))
            },
            onClick = { onSignOutClick() })
    }
}

@Composable
private fun MenuItemText(text: String) {
    Text(text = text,
        style = TextStyle(fontSize = 17.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    )
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    SettingsDialog(
        isExpanded = true,
        onDismiss = {},
        isVPNEnabled = true,
        onVpnClick = {},
        onSignOutClick = {})
}