package com.example.fickbookauthorhelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD3D3D3),
    secondary = Color(0xFFAFAFAF),
    tertiary = Color(0xFFAFAFAF),
    background = Color(0xFF523930),
    onBackground = Color(0xFFAFAFAF),
    errorContainer = Color(0xFFFFDFDF),
    onErrorContainer = Color(0xFF5A2222),
    error = Color(0xFF942424),
    onError = Color(0xFFDFCECE),
)

@Composable
fun FickbookAuthorHelperTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}