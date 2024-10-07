package com.example.fickbookauthorhelper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RootColorScheme = lightColorScheme(
    primary = Color(0xFFD2B48C),
    onPrimary = Color(0xFF492D28),
    secondary = Color(0xFFA98266),
    onSecondary = Color(0xFFF5F5DC),
    tertiary = Color(0xFFC19A6B),
    onTertiary = Color(0xFF3E2723),
    background = Color(0xFF5A3D26),
    onBackground = Color(0xFFECE5C9),
    surface = Color(0xFF8B5A2B),
    onSurface = Color(0xFFF5F5DC),
    error = Color(0xFFB22222),
    onError = Color(0xFFF5F5DC),
    primaryContainer = Color(0xFFA98266),
    onPrimaryContainer = Color(0xFFF5F5DC),
    secondaryContainer = Color(0xFF6B4226),
    onSecondaryContainer = Color(0xFFF5F5DC)
)

val MyShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(12.dp)
)

@Composable
fun FickbookAuthorHelperTheme(content: @Composable () -> Unit) {
    val colorScheme = RootColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MyShapes,
        typography = Typography,
        content = content
    )
}