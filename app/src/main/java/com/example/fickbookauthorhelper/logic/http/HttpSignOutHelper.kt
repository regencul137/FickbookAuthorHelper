package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

interface IHttpSignOutHelper {
    suspend fun signOut(): Result<Boolean>
}

@Singleton
class HttpSignOutHelper @Inject constructor(
    private val clientProvider: IClientProvider
) : IHttpSignOutHelper {
    override suspend fun signOut(): Result<Boolean> {
        val loginPath = "$FICBOOK_URL/logout"
        val request = Request.Builder().url(loginPath).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        try {
                            if (it.contains("register")) {
                                Result.success(true)
                            } else {
                                println("HttpSignOutHelper: error")
                                Result.success(false)
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