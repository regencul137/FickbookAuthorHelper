package com.example.fickbookauthorhelper.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.R

@Preview
@Composable
fun ConnectionFailedView(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = stringResource(R.string.error_connection_reset),
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = MaterialTheme.colorScheme.error,
                fontSize = 19.sp,
                fontWeight = FontWeight.Thin
            )
        )
    }
}