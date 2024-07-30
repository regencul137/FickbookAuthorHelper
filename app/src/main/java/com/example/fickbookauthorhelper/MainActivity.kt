package com.example.fickbookauthorhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.fickbookauthorhelper.ui.theme.FickbookAuthorHelperTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        UserBlock(user = model.currentUser)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserBlock(user: FHAuthManager.User) {
    Row {
        Text(text = user.name)
        user.avatar?.let {
            Image(painter = rememberDrawablePainter(drawable = it), contentDescription = "avatar")
        }
    }
}

@Preview
@Composable
private fun UserBlockPreview() {
    UserBlock(user = FHAuthManager.User("Username", LocalContext.current.getDrawable(android.R.drawable.star_on)))
}

@HiltViewModel
class MainViewModel @Inject constructor(private val authManager: FHAuthManager) : ViewModel() {
    val currentUser = authManager.currentUser
}

