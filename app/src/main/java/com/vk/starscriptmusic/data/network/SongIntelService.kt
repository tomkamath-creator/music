package com.vk.starscriptmusic.data.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class SongIntel(
    val releaseYear: String? = null,
    val recordLabel: String? = null,
    val genre: String? = null,
    val trivia: String? = null,
    val highResArtworkUrl: String? = null,
    val isOfflineFallback: Boolean = false
)

/**
 * Service for fetching song metadata, release info, high-res artwork, and trivia
 * via public iTunes Search API and Wikipedia Summary API with graceful offline fallback.
 */
@Singleton
class SongIntelService @Inject constructor(
    private val gson: Gson
) {

    private val cache = ConcurrentHashMap<String, SongIntel>()

    suspend fun getSongIntel(title: String, artist: String, album: String? = null): SongIntel = withContext(Dispatchers.IO) {
        val cleanTitle = cleanSearchTerm(title)
        val cleanArtist = cleanSearchTerm(artist)
        val cacheKey = "$cleanTitle::$cleanArtist"

        cache[cacheKey]?.let { return@withContext it }

        if (cleanTitle.isBlank() || cleanArtist.isBlank()) {
            return@withContext createOfflineFallback(album)
        }

        var releaseYear: String? = null
        var recordLabel: String? = null
        var genre: String? = null
        var highResArtworkUrl: String? = null
        var triviaSnippet: String? = null

        // 1. Fetch iTunes metadata and 1400x1400 artwork
        try {
            val query = URLEncoder.encode("$cleanTitle $cleanArtist", "UTF-8")
            val iTunesUrl = "https://itunes.apple.com/search?term=$query&media=music&entity=song&limit=1"
            val response = executeGet(iTunesUrl)
            if (response != null) {
                val json = gson.fromJson(response, JsonObject::class.java)
                val results = json.getAsJsonArray("results")
                if (results != null && results.size() > 0) {
                    val firstItem = results[0].asJsonObject

                    // Release Year
                    val releaseDate = firstItem.get("releaseDate")?.asString
                    if (!releaseDate.isNullOrBlank() && releaseDate.length >= 4) {
                        releaseYear = releaseDate.substring(0, 4)
                    }

                    // Genre & Collection/Label
                    genre = firstItem.get("primaryGenreName")?.asString
                    recordLabel = firstItem.get("collectionName")?.asString

                    // 1400x1400 High-Res Artwork
                    val art100 = firstItem.get("artworkUrl100")?.asString
                    if (!art100.isNullOrBlank()) {
                        highResArtworkUrl = art100
                            .replace("100x100bb.jpg", "1400x1400bb.jpg")
                            .replace("100x100bb", "1400x1400bb")
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful network failure
        }

        // 2. Fetch Wikipedia trivia / editorial excerpt
        try {
            val wikiTerm = URLEncoder.encode("${cleanTitle}_(song)", "UTF-8")
            val wikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$wikiTerm"
            val wikiResponse = executeGet(wikiUrl)
            if (wikiResponse != null) {
                val wikiJson = gson.fromJson(wikiResponse, JsonObject::class.java)
                val extract = wikiJson.get("extract")?.asString
                if (!extract.isNullOrBlank() && !extract.contains("may refer to", ignoreCase = true)) {
                    triviaSnippet = extract
                }
            }

            // Fallback Wikipedia query on artist if song query returned nothing
            if (triviaSnippet == null) {
                val artistTerm = URLEncoder.encode(cleanArtist, "UTF-8")
                val artistWikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$artistTerm"
                val artistResponse = executeGet(artistWikiUrl)
                if (artistResponse != null) {
                    val artistJson = gson.fromJson(artistResponse, JsonObject::class.java)
                    val extract = artistJson.get("extract")?.asString
                    if (!extract.isNullOrBlank() && !extract.contains("may refer to", ignoreCase = true)) {
                        triviaSnippet = extract
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful network failure
        }

        val result = if (releaseYear != null || recordLabel != null || genre != null || triviaSnippet != null || highResArtworkUrl != null) {
            SongIntel(
                releaseYear = releaseYear ?: "Unknown",
                recordLabel = recordLabel ?: album ?: "Independent",
                genre = genre ?: "Pop / Electronic",
                trivia = triviaSnippet ?: "High-fidelity local audio playback preserved offline.",
                highResArtworkUrl = highResArtworkUrl,
                isOfflineFallback = false
            )
        } else {
            createOfflineFallback(album)
        }

        cache[cacheKey] = result
        result
    }

    private fun createOfflineFallback(album: String?): SongIntel {
        return SongIntel(
            releaseYear = "Offline",
            recordLabel = album ?: "Local Storage",
            genre = "Studio Master",
            trivia = "Offline Intel: Playing directly from local storage. Embedded ID3 metadata active.",
            highResArtworkUrl = null,
            isOfflineFallback = true
        )
    }

    private fun cleanSearchTerm(input: String): String {
        return input
            .replace(Regex("""\([^)]*\)"""), "")
            .replace(Regex("""\[[^\]]*\]"""), "")
            .replace(Regex("""(?i)feat\..*"""), "")
            .replace(Regex("""(?i)ft\..*"""), "")
            .trim()
    }

    private fun executeGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "StarscriptMusic/1.0 (Android)")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else null
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
