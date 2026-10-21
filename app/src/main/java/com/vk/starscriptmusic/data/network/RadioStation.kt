package com.vk.starscriptmusic.data.network

import com.google.gson.annotations.SerializedName
import com.vk.starscriptmusic.domain.model.Track

/**
 * Data model for a radio station from the public Radio Browser API (radio-browser.info).
 */
data class RadioStation(
    @SerializedName("stationuuid")
    val stationuuid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("url_resolved")
    val urlResolved: String,

    @SerializedName("homepage")
    val homepage: String? = null,

    @SerializedName("favicon")
    val favicon: String? = null,

    @SerializedName("tags")
    val tags: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("countrycode")
    val countryCode: String? = null,

    @SerializedName("language")
    val language: String? = null,

    @SerializedName("votes")
    val votes: Int = 0,

    @SerializedName("codec")
    val codec: String? = null,

    @SerializedName("bitrate")
    val bitrate: Int = 0,

    @SerializedName("clickcount")
    val clickcount: Int = 0
) {
    /**
     * Converts the radio station to a domain Track model for Jetpack Media3 playback.
     */
    fun toTrack(): Track {
        val displayArtist = tags?.split(",")?.take(3)?.joinToString(", ")?.trim()
            ?.ifBlank { country ?: "Live Radio" } ?: (country ?: "Live Radio")

        return Track(
            id = stationuuid.hashCode().toLong(),
            title = name.trim(),
            artist = displayArtist,
            album = country?.ifBlank { "Global Stream" } ?: "Global Stream",
            durationMs = 0L,
            contentUri = urlResolved,
            albumArtUri = favicon?.ifBlank { null },
            dateAdded = System.currentTimeMillis(),
            size = 0L,
            bitrate = bitrate,
            mimeType = codec
        )
    }
}
