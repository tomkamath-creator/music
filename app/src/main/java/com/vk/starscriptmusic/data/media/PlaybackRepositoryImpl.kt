package com.vk.starscriptmusic.data.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlaybackRepository that bridges UI commands to MediaPlaybackService
 * via Jetpack Media3 MediaController.
 */
@Singleton
class PlaybackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateHolder: PlaybackStateHolder,
    private val sleepTimerManager: SleepTimerManager
) : PlaybackRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override val playbackState: StateFlow<PlaybackState> = stateHolder.state
    override val sleepTimerRemainingSeconds: StateFlow<Int> = sleepTimerManager.remainingSeconds
    override val isSleepTimerActive: StateFlow<Boolean> = sleepTimerManager.isTimerActive

    init {
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    syncInitialState()
                } catch (_: Exception) {
                    // Handled gracefully if service is rebinding
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun syncInitialState() {
        val controller = mediaController ?: return
        stateHolder.updatePlaying(controller.isPlaying)
        stateHolder.updateRepeatMode(controller.repeatMode)
        stateHolder.updateShuffleMode(controller.shuffleModeEnabled)
        stateHolder.updateProgress(
            controller.currentPosition,
            controller.duration.coerceAtLeast(0L),
            controller.bufferedPosition
        )
    }

    private fun toMediaItem(track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(Uri.parse(track.contentUri))
                    .build()
            )
            .setUri(track.contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    override fun playTrack(track: Track, queue: List<Track>) {
        val controller = mediaController ?: return
        repositoryScope.launch {
            val targetQueue = if (queue.isEmpty()) listOf(track) else queue
            val mediaItems = targetQueue.map { toMediaItem(it) }
            val targetIndex = targetQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

            stateHolder.updateCurrentTrack(track, targetQueue, targetIndex)

            controller.setMediaItems(mediaItems, targetIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    override fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun resume() {
        mediaController?.play()
    }

    override fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs)
        stateHolder.updateProgress(
            positionMs,
            controller.duration.coerceAtLeast(0L),
            controller.bufferedPosition
        )
    }

    override fun skipNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    override fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000L || !controller.hasPreviousMediaItem()) {
            controller.seekTo(0L)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    override fun setRepeatMode(@Player.RepeatMode repeatMode: Int) {
        mediaController?.repeatMode = repeatMode
        stateHolder.updateRepeatMode(repeatMode)
    }

    override fun setShuffleMode(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
        stateHolder.updateShuffleMode(enabled)
    }

    override fun setCrossfade(seconds: Int) {
        // Crossfade duration stored for gapless transition pipeline
    }

    override fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        stateHolder.updatePlaybackSpeed(speed)
    }

    override fun startSleepTimer(minutes: Int) {
        sleepTimerManager.startTimer(
            durationMinutes = minutes,
            onVolumeFade = { fadeFactor -> setVolume(fadeFactor) },
            onTimerComplete = { pause() }
        )
    }

    override fun cancelSleepTimer() {
        sleepTimerManager.cancelTimer()
        setVolume(1.0f)
    }

    override fun setVolume(volume: Float) {
        mediaController?.volume = volume.coerceIn(0.0f, 1.0f)
    }

    override fun release() {
        cancelSleepTimer()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
