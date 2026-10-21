package com.vk.starscriptmusic.domain.model

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Domain representation of an audio track.
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val size: Long,
    val bitrate: Int? = null,
    val mimeType: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    /**
     * Formats duration in milliseconds into "m:ss" or "h:mm:ss".
     */
    fun formattedDuration(): String {
        if (durationMs <= 0) return "0:00"
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }
}
