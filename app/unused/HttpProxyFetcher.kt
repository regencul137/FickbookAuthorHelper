package com.example.fickbookauthorhelper.logic.http

import com.example.fickbookauthorhelper.logic.ProxyServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Random
import javax.inject.Inject

interface IHttpProxyFetcher {
    suspend fun fetchProxies(client: OkHttpClient): Result<List<ProxyServer>>
}

class HttpProxyFetcher @Inject constructor() : IHttpProxyFetcher {
    companion object {
        private const val PROXY_LIST_ADVANCED_NAME = "https://advanced.name/freeproxy?type=https"
        private const val PROXY_LIST_PROXY_DB_NET = "https://proxydb.net/?protocol=https&protocol=http"
        private const val PROXY_LIST_FREE_PROXY_CZ_HTTP = "http://free-proxy.cz/en/proxylist/country/all/http/ping/all"
        private const val PROXY_LIST_FREE_PROXY_CZ_HTTPS =
            "http://free-proxy.cz/en/proxylist/country/all/https/ping/all"
        private const val PROXY_LIST_GITHUB_LIST_URL =
            "https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt"
        private const val PROXY_LIST_URL_GEONODE =
            "https://proxylist.geonode.com/api/proxy-list?" +
                    "protocols=https%2Chttp&" +
                    "limit=500&page=1&" +
                    "sort_by=lastChecked&" +
                    "sort_type=desc"
        private const val PROXY_LIST_URL_HIDE_NAME = "https://hidxx.name/proxy-list/?type=s#list"
    }

    override suspend fun fetchProxies(client: OkHttpClient): Result<List<ProxyServer>> {
        println("HttpProxyFetcher fetchProxies")
        return withContext(Dispatchers.IO) {
            try {
                val proxies = arrayListOf<ProxyServer>()

                proxies.addAll(fetchFromSite(client, PROXY_LIST_ADVANCED_NAME, ::parseAdvancedNameProxyList))
                proxies.addAll(fetchFromSite(client, PROXY_LIST_PROXY_DB_NET, ::parseProxyDbNet))
                proxies.addAll(fetchFromFreeProxyCzWithPagination(client, PROXY_LIST_FREE_PROXY_CZ_HTTP))
                proxies.addAll(fetchFromFreeProxyCzWithPagination(client, PROXY_LIST_FREE_PROXY_CZ_HTTPS))
                proxies.addAll(fetchFromSite(client, PROXY_LIST_URL_HIDE_NAME, ::parseHidemynameProxyList))
                proxies.addAll(fetchFromSite(client, PROXY_LIST_URL_GEONODE, ::parseGeonodeProxyList))

                proxies.removeIf {
                    it.country.equals("ru", ignoreCase = true) ||
                            it.country.equals("russian federation", ignoreCase = true)
                }

                if (proxies.isEmpty()) Result.failure(Exception("No proxies!")) else Result.success(proxies)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchFromSite(
        client: OkHttpClient,
        url: String,
        parse: (body: String) -> List<ProxyServer>
    ): List<ProxyServer> {
        try {
            return withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val result = responseBody?.let { parse(it) } ?: listOf()
                println("HttpProxyFetcher fetchFromSite $url: ${result.size}")
                result.shuffled(Random())
            }
        } catch (e: Exception) {
            println("HttpProxyFetcher fetchFromSite $url was failed: ${e.cause}")
            return listOf()
        }
    }

    private suspend fun fetchFromFreeProxyCzWithPagination(
        client: OkHttpClient,
        baseUrl: String
    ): List<ProxyServer> {
        val allProxies = arrayListOf<ProxyServer>()
        try {
            return withContext(Dispatchers.IO) {
                val request = Request.Builder().url(baseUrl).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext allProxies
                }
                val maxPage = 5
                for (page in 1..maxPage) {
                    val url = if (page == 1) baseUrl else "$baseUrl/$page"
                    val pageRequest = Request.Builder().url(url).build()
                    val pageResponse = client.newCall(pageRequest).execute()
                    val pageBody = pageResponse.body?.string()
                    if (pageBody.isNullOrEmpty()) {
                        break
                    }
                    val proxies = parseFreeProxyCzList(pageBody)
                    if (proxies.isEmpty()) {
                        break
                    }
                    allProxies.addAll(proxies)
                }
                println("HttpProxyFetcher fetchFromSite $baseUrl: ${allProxies.size}")
                allProxies.shuffled(Random())
            }
        } catch (e: Exception) {
            println("HttpProxyFetcher fetchFromSite $baseUrl corrupted with failure: ${e.cause}")
            println("HttpProxyFetcher fetchFromSite $baseUrl: ${allProxies.size}")
            return allProxies
        }
    }

    private fun parseHidemynameProxyList(html: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val document = Jsoup.parse(html)
        val rows = document.select("div.table_block tbody tr")

        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 2) {
                val ip = cells[0].text()
                val port = cells[1].text().toIntOrNull() ?: continue
                val country = cells[2].select("span.country").text()
                proxyItems.add(ProxyServer(ip, port, country))
            }
        }

        return proxyItems
    }

    private fun parseGeonodeProxyList(json: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val jsonObject = JSONObject(json)
        val dataArray: JSONArray = jsonObject.getJSONArray("data")

        for (i in 0 until dataArray.length()) {
            val proxyJson = dataArray.getJSONObject(i)
            val ip = proxyJson.getString("ip")
            val port = proxyJson.getInt("port")
            val country = proxyJson.optString("country", "Unknown")
            proxyItems.add(ProxyServer(ip, port, country))
        }

        return proxyItems
    }

    private fun parseGithubProxies(body: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val lines = body.lines()
        for (line in lines) {
            val parts = line.split(":")
            if (parts.size == 2) {
                val ip = parts[0].trim()
                val port = parts[1].toIntOrNull() ?: continue
                proxyItems.add(ProxyServer(ip, port, "Unknown")) // Country is unknown in this case
            }
        }
        return proxyItems
    }

    private fun parseFreeProxyCzList(html: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val document = Jsoup.parse(html)
        val rows = document.select("table#proxy_list tbody tr")
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 8) {
                val encodedIp = cells[0].select("script").html()
                val decodedIp = decodeBase64Ip(encodedIp) // Decode the IP
                val port = cells[1].text().toIntOrNull() ?: continue
                val country = cells[3].select("span").attr("title")

                proxyItems.add(ProxyServer(decodedIp, port, country))
            }
        }
        return proxyItems
    }

    private fun parseProxyDbNet(html: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val document = Jsoup.parse(html)
        val rows = document.select("table tbody tr")
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 2) {
                val ipPort = cells[0].text().split(":")
                if (ipPort.size == 2) {
                    val ip = ipPort[0]
                    val port = ipPort[1].toIntOrNull() ?: continue
                    val country = cells[2].text()
                    proxyItems.add(ProxyServer(ip, port, country))
                }
            }
        }
        return proxyItems
    }

    private fun parseAdvancedNameProxyList(html: String): List<ProxyServer> {
        val proxyItems = mutableListOf<ProxyServer>()
        val document = Jsoup.parse(html)
        val rows = document.select("table tbody tr")

        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 2) {
                val ip = cells[0].text()
                val port = cells[1].text().toIntOrNull() ?: continue
                val country = cells[3].text()
                proxyItems.add(ProxyServer(ip, port, country))
            }
        }

        return proxyItems
    }

    private fun decodeBase64Ip(encodedIp: String): String {
        val base64Regex = "Base64.decode\\(\"(.*?)\"\\)".toRegex()
        val matchResult = base64Regex.find(encodedIp)
        val base64String = matchResult?.groups?.get(1)?.value ?: return ""
        return String(android.util.Base64.decode(base64String, android.util.Base64.DEFAULT))
    }
}
