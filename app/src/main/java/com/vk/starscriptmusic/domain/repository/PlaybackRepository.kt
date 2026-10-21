package com.vk.starscriptmusic.domain.repository

import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean Architecture Interface for interacting with the audio playback engine.
 */
interface PlaybackRepository {

    val playbackState: StateFlow<PlaybackState>

    fun playTrack(track: Track, queue: List<Track> = listOf(track))

    fun playPause()

    fun pause()

    fun resume()

    fun seekTo(positionMs: Long)

    fun seekRelative(offsetMs: Long)

    fun skipNext()

    fun skipPrevious()

    fun setRepeatMode(repeatMode: Int)

    fun setShuffleMode(enabled: Boolean)

    fun setCrossfade(seconds: Int)

    fun setPlaybackSpeed(speed: Float)

    fun startSleepTimer(minutes: Int)

    fun cancelSleepTimer()

    val sleepTimerRemainingSeconds: StateFlow<Int>

    val isSleepTimerActive: StateFlow<Boolean>

    fun setVolume(volume: Float)

    fun release()
}
