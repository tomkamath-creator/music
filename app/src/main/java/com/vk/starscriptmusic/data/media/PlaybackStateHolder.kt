package com.vk.starscriptmusic.data.media

import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe singleton StateFlow holder for the playback engine state.
 */
@Singleton
class PlaybackStateHolder @Inject constructor() {

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    fun updateCurrentTrack(track: Track?, queue: List<Track>, queueIndex: Int) {
        _state.update {
            it.copy(
                currentTrack = track,
                queue = queue,
                currentQueueIndex = queueIndex,
                durationMs = track?.durationMs ?: 0L
            )
        }
    }

    fun updatePlaying(isPlaying: Boolean) {
        _state.update { it.copy(isPlaying = isPlaying) }
    }

    fun updateProgress(positionMs: Long, durationMs: Long, bufferedMs: Long) {
        _state.update {
            it.copy(
                currentPositionMs = positionMs,
                durationMs = if (durationMs > 0) durationMs else it.durationMs,
                bufferedPositionMs = bufferedMs
            )
        }
    }

    fun updateRepeatMode(repeatMode: Int) {
        _state.update { it.copy(repeatMode = repeatMode) }
    }

    fun updateShuffleMode(enabled: Boolean) {
        _state.update { it.copy(shuffleModeEnabled = enabled) }
    }

    fun updatePlaybackSpeed(speed: Float) {
        _state.update { it.copy(playbackSpeed = speed) }
    }

    fun updateDynamicPalette(primaryArgb: Int?, secondaryArgb: Int?) {
        _state.update {
            it.copy(
                primaryAccentArgb = primaryArgb,
                secondaryAccentArgb = secondaryArgb
            )
        }
    }

    fun reset() {
        _state.value = PlaybackState()
    }
}
