package com.example.fickbookauthorhelper.logic

import android.graphics.drawable.Drawable
import com.example.fickbookauthorhelper.FHApplication.Companion.AVATAR_PATH
import com.example.fickbookauthorhelper.logic.http.IHttpUserLoadHelperUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface IUserManager {
    suspend fun loadUser(): Result<Boolean>
    val username: StateFlow<String?>
    val avatar: StateFlow<Drawable?>
}

class UserManager @Inject constructor(
    private val userLoadHelper: IHttpUserLoadHelperUser,
    private val imageLoader: ImageLoader,
    private val eventEmitter: IEventEmitter
) : IUserManager {
    sealed class Event : IEvent {
        data class UserNameLoaded(val name: String) : Event()
        data class UserAvatarLoaded(val avatar: Drawable?) : Event()
    }

    private val _username = MutableStateFlow<String?>(null)
    override val username: StateFlow<String?> get() = _username

    private val _avatar = MutableStateFlow<Drawable?>(null)
    override val avatar: StateFlow<Drawable?> get() = _avatar

    override suspend fun loadUser(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val result = userLoadHelper.loadUser().getOrNull()
                result?.let {
                    _username.value = result.username
                    eventEmitter.pushEvent(Event.UserNameLoaded(result.username))

                    val imagePath = imageLoader.downloadImage(result.avatarUrl, AVATAR_PATH, "avatar.jpg")
                    imagePath?.let { path ->
                        val drawableAvatar = Drawable.createFromPath(path)
                        _avatar.value = drawableAvatar
                        eventEmitter.pushEvent(Event.UserAvatarLoaded(drawableAvatar))
                        Result.success(true)
                    } ?: Result.success(false)
                } ?: Result.success(false)
            }.getOrElse { exception ->
                Result.failure(exception)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UserManagerModule {
    @Singleton
    @Provides
    fun provideUserManager(
        userLoadHelper: IHttpUserLoadHelperUser,
        imageLoader: ImageLoader,
        eventEmitter: IEventEmitter
    ): IUserManager {
        return UserManager(userLoadHelper, imageLoader, eventEmitter)
    }
}
