package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.FHApplication.Companion.FICBOOK_URL
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import javax.inject.Inject

interface IHttpUserLoadHelperUser {
    suspend fun loadUser(): Result<UserInfo?>
}

data class UserInfo(val username: String, val avatarUrl: String)

class HttpUserLoadHelper @Inject constructor(
    private val clientProvider: IClientProvider
) : IHttpUserLoadHelperUser {
    override suspend fun loadUser(): Result<UserInfo?> {
        val request = Request.Builder()
            .url(FICBOOK_URL)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientProvider.client().newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.success(null)
                    Result.success(parseUserInfo(body))
                } else {
                    Result.success(null)
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    private fun parseUserInfo(html: String): UserInfo? {
        val doc: Document = Jsoup.parse(html)
        val userNameElement: Element? = doc.selectFirst("div.dropdown.profile-holder span.text.hidden-xs")
        val avatarElement: Element? = doc.selectFirst("div.avatar-cropper img")

        return if (userNameElement != null && avatarElement != null) {
            val userName = userNameElement.text()
            val avatarUrl = avatarElement.attr("src")
            UserInfo(userName, avatarUrl)
        } else {
            null
        }
    }
}