package com.example.fickbookauthorhelper.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    title: String
) {
    Column(
        modifier = modifier
            .width(300.dp)
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.large
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingViewPreview() {
    FickbookAuthorHelperTheme {
        LoadingView(title = "Loading", modifier = Modifier.padding(16.dp))
    }
}