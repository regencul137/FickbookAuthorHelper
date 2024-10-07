package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.IEvent
import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

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
    private val clientProvider: IClientProvider,
    private val eventEmitter: IEventEmitter
) : IHttpSignInHelper {
    sealed class Event : IEvent {
        data object SignedInEvent : Event()
    }

    override suspend fun signIn(username: String, password: String): Result<Unit> {
        val loginPath = "$FICBOOK_URL/login_check"

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
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        try {
                            val responseJson = JSONObject(it)
                            if (responseJson.optBoolean("result", false)) {
                                eventEmitter.pushEvent(Event.SignedInEvent)
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