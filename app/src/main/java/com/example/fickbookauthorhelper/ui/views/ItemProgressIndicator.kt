package com.example.fickbookauthorhelper.ui.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ItemProgressIndicator(color: Color = MaterialTheme.colorScheme.primary) {
    CircularProgressIndicator(
        color = color,
        strokeWidth = 2.dp,
        modifier = Modifier.size(36.dp)
    )
}