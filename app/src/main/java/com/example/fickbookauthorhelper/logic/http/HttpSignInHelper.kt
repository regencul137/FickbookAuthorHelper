package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICKBOOK_URL
import com.example.fickbookauthorhelper.logic.FHEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

interface IHttpSignInHelper {
    suspend fun signIn(username: String, password: String): Result<Unit>

    sealed class SignInException(message: String) : Exception(message) {
        // Request was not executed
        data object ConnectionResetException : SignInException("Connection was reset, maybe no VPN") {
            private fun readResolve(): Any = ConnectionResetException
        }

        data object TimeOutException : SignInException("Request timed out") {
            private fun readResolve(): Any = TimeOutException
        }

        data object UnknownBackendException : SignInException("Unknown backend exception") {
            private fun readResolve(): Any = UnknownBackendException
        }

        // Request was done
        data object ParseException : SignInException("Error parsing result") {
            private fun readResolve(): Any = ParseException
        }

        data object UnknownSignInException : SignInException("Unknown sign-in error, request has no body") {
            private fun readResolve(): Any = UnknownSignInException
        }

        data object InvalidCredentialsException : SignInException("Invalid credentials") {
            private fun readResolve(): Any = InvalidCredentialsException
        }

        data object UserNotFoundException : SignInException("User not found") {
            private fun readResolve(): Any = UserNotFoundException
        }

        data class RequestException(val code: Int) : SignInException("Request error with code: $code")
    }

    companion object {
        object RequestErrors {
            const val INVALID_CREDENTIALS = "invalid_credentials"
            const val USER_NOT_FOUND = "user_not_found"
        }
    }
}

class HttpSignInHelper @Inject constructor(
    private val clientProvider: IHttpClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpSignInHelper {
    override suspend fun signIn(username: String, password: String): Result<Unit> {
        val loginPath = "$FICKBOOK_URL/login_check"

        val formBody = FormBody.Builder()
            .add("login", username)
            .add("password", password)
            .add("remember", "true")
            .build()

        val request = Request.Builder()
            .url(loginPath)
            .post(formBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        try {
                            val responseJson = JSONObject(it)
                            if (responseJson.optBoolean("result", false)) {
                                eventEmitter.emit(FHEvent.SignedInEvent)
                                Result.success(Unit)
                            } else {
                                val errorJson = responseJson.getJSONObject("error")
                                when (errorJson.optString("reason", "")) {
                                    IHttpSignInHelper.Companion.RequestErrors.USER_NOT_FOUND -> {
                                        Result.failure(IHttpSignInHelper.SignInException.UserNotFoundException)
                                    }

                                    IHttpSignInHelper.Companion.RequestErrors.INVALID_CREDENTIALS -> {
                                        Result.failure(IHttpSignInHelper.SignInException.InvalidCredentialsException)
                                    }

                                    else -> {
                                        Result.failure(IHttpSignInHelper.SignInException.UnknownSignInException)
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            Result.failure(IHttpSignInHelper.SignInException.ParseException)
                        }
                    } ?: run {
                        Result.failure(IHttpSignInHelper.SignInException.UnknownSignInException)
                    }
                } else {
                    Result.failure(IHttpSignInHelper.SignInException.RequestException(response.code))
                }
            } catch (exception: SocketException) {
                Result.failure(IHttpSignInHelper.SignInException.ConnectionResetException)
            } catch (exception: IOException) {
                Result.failure(IHttpSignInHelper.SignInException.UnknownBackendException)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SignInHelperModule {
    @Singleton
    @Provides
    fun provideSignInHelper(clientProvider: IHttpClientProvider, eventEmitter: IEventEmitter): HttpSignInHelper {
        return HttpSignInHelper(clientProvider, eventEmitter)
    }

    @Singleton
    @Provides
    fun provideISignInHelper(signInHelper: HttpSignInHelper): IHttpSignInHelper = signInHelper
}
