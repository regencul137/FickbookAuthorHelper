package com.example.fickbookauthorhelper.ui.signIn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fickbookauthorhelper.R
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme

@Composable
fun SignInView(
    modifier: Modifier = Modifier,
    model: SignInViewModel = viewModel()
) {
    DisposableEffect(Unit) {
        onDispose {
            model.onDispose()
        }
    }

    val state by model.state.collectAsState()
    val login by model.login.observeAsState("")
    val password by model.password.observeAsState("")
    val rememberMe by model.rememberMe.observeAsState(initial = false)

    SignInView(
        modifier = modifier,
        state = state,
        login = login,
        password = password,
        rememberMe = rememberMe,
        onUsernameChanged = { model.onLoginChange(it) },
        onPasswordChanged = { model.onPasswordChange(it) },
        onRememberMeClick = { model.onRememberMeChange(!rememberMe) },
        onSignInClick = { model.signIn() })
}

@Composable
private fun SignInView(
    modifier: Modifier = Modifier,
    state: SignInViewModel.State,
    login: String,
    password: String,
    rememberMe: Boolean,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var isInputEnabled: Boolean by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorText: String? by remember { mutableStateOf(null) }

    val focusedColor = MaterialTheme.colorScheme.onPrimary
    val unfocusedColor = MaterialTheme.colorScheme.secondary
    val fieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = focusedColor,
        focusedTextColor = focusedColor,
        focusedLabelColor = focusedColor,
        focusedBorderColor = focusedColor,
        focusedTrailingIconColor = focusedColor,
        unfocusedTextColor = unfocusedColor,
        unfocusedLabelColor = unfocusedColor,
        unfocusedBorderColor = unfocusedColor,
        unfocusedTrailingIconColor = unfocusedColor,
        disabledBorderColor = unfocusedColor,
        disabledTrailingIconColor = unfocusedColor,
        disabledLabelColor = unfocusedColor,
        disabledTextColor = unfocusedColor
    )

    val contentColor = Color.White.copy(alpha = 0.5f)
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedContentColor by infiniteTransition.animateColor(
        initialValue = contentColor,
        targetValue = contentColor.copy(alpha = 0.4f),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    when (state) {
        is SignInViewModel.State.Error -> {
            isInputEnabled = true
            errorText = stringResource(id = state.messageId)
        }

        SignInViewModel.State.Input -> {
            isInputEnabled = true
            errorText = null
        }

        SignInViewModel.State.RequestInProcess -> {
            isInputEnabled = false
            errorText = null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                color = if (state == SignInViewModel.State.RequestInProcess) animatedContentColor else contentColor,
                shape = MaterialTheme.shapes.medium
            )
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.sign_in),
            modifier = Modifier.height(200.dp),
            contentDescription = ""
        )
        OutlinedTextField(
            value = login,
            label = { Text(stringResource(R.string.username)) },
            colors = fieldColors,
            onValueChange = { onUsernameChanged(it) },
            enabled = isInputEnabled
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChanged(it) },
            enabled = isInputEnabled,
            label = { Text(stringResource(R.string.password)) },
            colors = fieldColors,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Outlined.VisibilityOff

                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.remember_me),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600
                )
            )
            Checkbox(
                checked = rememberMe,
                enabled = isInputEnabled,
                colors = CheckboxDefaults.colors(
                    disabledCheckedColor = unfocusedColor,
                    disabledUncheckedColor = unfocusedColor
                ),
                onCheckedChange = { onRememberMeClick() })
        }
        AnimatedVisibility(visible = rememberMe) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colorScheme.onError,
                    contentDescription = "",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your login data will be securely stored on your device using encryption",
                    style = TextStyle(color = MaterialTheme.colorScheme.onError)
                )
            }
        }
        Button(
            onClick = { onSignInClick() },
            shape = MaterialTheme.shapes.large,
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            ),
            enabled = isInputEnabled
        ) {
            Text(
                text = stringResource(R.string.sign_in).uppercase(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )
            )
        }
        AnimatedVisibility(
            visible = errorText != null,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            errorText?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFC5B295)
@Composable
private fun SignInPreview() {
    FickbookAuthorHelperTheme {
        SignInView(
            modifier = Modifier.padding(25.dp),
            state = SignInViewModel.State.Input,
            login = "",
            password = "",
            rememberMe = true,
            onUsernameChanged = {},
            onPasswordChanged = {},
            onRememberMeClick = {},
            onSignInClick = {}
        )
    }
}