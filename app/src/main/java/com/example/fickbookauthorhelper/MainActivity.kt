package com.example.fickbookauthorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FickbookAuthorHelperTheme {
                Content(model)
            }
        }
    }
}

@Composable
private fun Content(model: IMainViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        top = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateTopPadding()
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                UserBlock(user = model.currentUser)
            }
        }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
        ) {

        }
    }
}

@Composable
private fun UserBlock(
    user: FHAuthManager.User,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = user.avatarId),
            contentDescription = "avatar",
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = user.name,
            maxLines = 1,
            style = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 21.sp
            )
        )
    }
}

interface IMainViewModel {
    val currentUser: FHAuthManager.User
}

@HiltViewModel
class MainViewModel @Inject constructor(private val authManager: FHAuthManager) : ViewModel(), IMainViewModel {
    override val currentUser = authManager.currentUser
}

private val mockUser = FHAuthManager.User(
    "Username",
    R.drawable.ic_default_avatar
)

private val mockPageModel = object : IMainViewModel {
    override val currentUser: FHAuthManager.User
        get() = mockUser

}

@Preview
@Composable
private fun ContentPreview() {
    FickbookAuthorHelperTheme {
        Content(mockPageModel)
    }
}

@Preview
@Composable
private fun UserBlockPreview() {
    FickbookAuthorHelperTheme {
        UserBlock(
            user = mockUser
        )
    }
}

