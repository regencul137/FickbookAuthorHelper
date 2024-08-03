import com.example.fickbookauthorhelper.FHAuthManager
import com.example.fickbookauthorhelper.IAuthManager
import com.example.fickbookauthorhelper.MainViewModel
import com.example.fickbookauthorhelper.R

private val mockAuthManager: IAuthManager = object : IAuthManager {
    override val currentUser: FHAuthManager.User
        get() = mockUser
    override val isSignedIn: Boolean
        get() = true
}

val mockUser = FHAuthManager.User(
    "Username",
    R.drawable.ic_default_avatar
)

val mockPageModel: MainViewModel = MainViewModel(mockAuthManager)