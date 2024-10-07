package com.example.fickbookauthorhelper.ui.views

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme

@Composable
fun RequestPermissionsView(
    modifier: Modifier = Modifier,
    permissions: List<String>,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var checkingIndex by remember { mutableStateOf<Int?>(null) }
    var needToShowHint by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
                needToShowHint = false
            } else {
                checkingIndex?.let { index ->
                    if (index in permissions.indices) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                context as ComponentActivity,
                                permissions[checkingIndex ?: 0]
                            )
                        ) {
                            needToShowHint = true
                        }
                    }
                }
            }
            checkingIndex = null
        }
    )

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.permissions_title),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.W500
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        permissions.forEachIndexed { index, permission ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val permissionName = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> stringResource(R.string.post_notifications)
                    else -> ""
                }
                Text(
                    text = permissionName,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                )
                Switch(
                    checked = checkingIndex == index,
                    onCheckedChange = {
                        checkingIndex = index
                        permissionLauncher.launch(permissions[index])
                    })
            }
            if (index != permissions.size - 1) {
                HorizontalDivider(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    thickness = 0.5.dp
                )
            }
        }
        AnimatedVisibility(visible = needToShowHint) {
            val annotatedText = buildAnnotatedString {
                append(stringResource(id = R.string.permission_hint_part_1) + " ")
                pushStringAnnotation(tag = "SETTINGS", annotation = "openSettings")
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "SETTINGS",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        })
                ) {
                    append(stringResource(id = R.string.permission_hint_part_2))
                }
                pop()
                append(" " + stringResource(id = R.string.permission_hint_part_3))
            }
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = annotatedText,
                    style = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview
@Composable
private fun RequestPermissionsPreview() {
    FickbookAuthorHelperTheme {
        RequestPermissionsView(
            permissions = listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            onPermissionGranted = {}
        )
    }
}