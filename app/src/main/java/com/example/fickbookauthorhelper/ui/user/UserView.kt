package com.example.fickbookauthorhelper.ui.user

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.settings.SettingsDialog
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.example.fickbookauthorhelper.ui.views.ItemProgressIndicator
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun UserView(
    modifier: Modifier = Modifier,
    contentColor: Color,
    model: UserViewModel = viewModel()
) {
    val userName by model.username.observeAsState("")
    val userAvatar by model.avatar.observeAsState(null)

    UserView(
        modifier = modifier,
        contentColor = contentColor,
        userName = userName,
        userAvatar = userAvatar
    )
}

@Composable
private fun UserView(
    modifier: Modifier = Modifier,
    contentColor: Color,
    userName: String?,
    userAvatar: Drawable?
) {
    var isExpanded  by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        UserBlock(userName, userAvatar, contentColor)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = contentColor
                )
            }
            SettingsDialog(
                isExpanded = isExpanded,
                onDismiss = { isExpanded = false }
            )
        }
    }
}

@Composable
private fun UserBlock(
    userName: String?,
    userAvatar: Drawable?,
    contentColor: Color
) {
    userAvatar?.let {
        Image(
            painter = rememberDrawablePainter(drawable = it),
            contentDescription = "avatar",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )
    } ?: run {
        ItemProgressIndicator(contentColor)
    }
    Spacer(modifier = Modifier.width(12.dp))
    userName?.let {
        Text(
            text = userName,
            maxLines = 1,
            style = TextStyle(
                color = contentColor,
                fontSize = 21.sp
            )
        )
    } ?: run {
        Text(
            text = stringResource(R.string.loading),
            maxLines = 1,
            style = TextStyle(
                color = contentColor,
                fontSize = 21.sp
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF6B4226)
@Composable
private fun UserViewPreview() {
    FickbookAuthorHelperTheme {
        UserView(contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            userName = "Username",
            userAvatar = null)
    }
}