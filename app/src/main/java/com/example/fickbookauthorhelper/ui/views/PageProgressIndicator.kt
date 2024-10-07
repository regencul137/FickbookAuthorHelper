package com.example.fickbookauthorhelper.ui.views

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme

@Composable
fun PageProgressIndicator(modifier: Modifier = Modifier, color: Color) {
    CircularProgressIndicator(
        color = color,
        strokeWidth = 3.dp,
        strokeCap = StrokeCap.Square,
        modifier = modifier.width(54.dp)
    )
}

@Preview
@Composable
private fun PageProgressIndicatorPreview() {
    FickbookAuthorHelperTheme {
        PageProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
    }
}