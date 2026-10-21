package com.vk.starscriptmusic.data.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for querying free public internet radio stations via the Radio Browser API (radio-browser.info).
 */
@Singleton
class RadioBrowserService @Inject constructor(
    private val gson: Gson
) {

    private val mirrors = listOf(
        "https://de1.api.radio-browser.info",
        "https://nl1.api.radio-browser.info",
        "https://at1.api.radio-browser.info"
    )

    suspend fun getTopStations(limit: Int = 40): List<RadioStation> = withContext(Dispatchers.IO) {
        fetchStations("/json/stations/topclick/$limit")
    }

    suspend fun getStationsByGenre(genre: String, limit: Int = 30): List<RadioStation> = withContext(Dispatchers.IO) {
        val encodedTag = URLEncoder.encode(genre.lowercase().trim(), "UTF-8")
        fetchStations("/json/stations/bytag/$encodedTag?limit=$limit&order=clickcount&reverse=true")
    }

    suspend fun searchStations(query: String, limit: Int = 30): List<RadioStation> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        fetchStations("/json/stations/byname/$encodedQuery?limit=$limit&order=clickcount&reverse=true")
    }

    private fun fetchStations(endpoint: String): List<RadioStation> {
        for (mirror in mirrors) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$mirror$endpoint")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("User-Agent", "StarscriptMusic/1.0 (Android)")
                    setRequestProperty("Accept", "application/json")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val type = object : TypeToken<List<RadioStation>>() {}.type
                    val list: List<RadioStation>? = gson.fromJson(reader, type)
                    reader.close()
                    if (!list.isNullOrEmpty()) {
                        // Filter out stations with empty or broken resolved URLs
                        return list.filter { it.urlResolved.isNotBlank() && it.name.isNotBlank() }
                    }
                }
            } catch (_: Exception) {
                // Try next mirror
            } finally {
                connection?.disconnect()
            }
        }
        return emptyList()
    }
}
