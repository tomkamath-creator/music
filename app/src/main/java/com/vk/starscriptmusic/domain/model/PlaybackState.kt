package com.vk.starscriptmusic.domain.model

import androidx.media3.common.Player

/**
 * Reactive playback snapshot emitted to the UI layer.
 */
data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    @Player.RepeatMode val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleModeEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1,
    val primaryAccentArgb: Int? = null,
    val secondaryAccentArgb: Int? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}
