package com.example.fickbookauthorhelper.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme

@Composable
fun TitleView(
    modifier: Modifier = Modifier,
    title: String,
    isExpanded: Boolean,
    detailsComposable: @Composable () -> Unit,
    onExpandClick: () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { onExpandClick() }
                .fillMaxWidth()
                .height(42.dp)
                .background(color = containerColor.copy(alpha = 0.55f))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                tint = contentColor,
                contentDescription = "expanded_animation"
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                style = TextStyle(
                    color = contentColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.W300
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            detailsComposable()
        }
    }
}

@Preview
@Composable
private fun TitlePreview() {
    FickbookAuthorHelperTheme {
        TitleView(title = "Title", isExpanded = true, detailsComposable = {}) {}
    }
}