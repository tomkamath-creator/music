package com.vk.starscriptmusic.data.media

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.vk.starscriptmusic.core.common.Constants
import com.vk.starscriptmusic.data.local.db.StarscriptDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Android 14+ Jetpack Media3 Background Playback Service.
 * Hosts ExoPlayer, connects with system lock-screen media controls,
 * and tracks audio listening metrics for the Top 50 Leaderboard.
 */
@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    @Inject
    lateinit var database: StarscriptDatabase

    @Inject
    lateinit var playbackStateHolder: PlaybackStateHolder

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressTrackingJob: Job? = null

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    private lateinit var notificationManager: MediaNotificationManager
    private lateinit var becomingNoisyReceiver: AudioBecomingNoisyReceiver

    // Play count calculation state
    private var activeTrackId: Long? = null
    private var hasCountedCurrentPlay: Boolean = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = MediaNotificationManager(this)

        initializePlayer()
        initializeMediaSession()
        setupNotificationProvider()
        setupNoisyReceiver()
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(PlayerEventListener())
    }

    private fun initializeMediaSession() {
        val sessionActivityIntent = notificationManager.createSessionActivityPendingIntent()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(MediaSessionCallback())
            .build()
    }

    private fun setupNotificationProvider() {
        setMediaNotificationProvider(notificationManager.createNotificationProvider())
    }

    private fun setupNoisyReceiver() {
        becomingNoisyReceiver = AudioBecomingNoisyReceiver(this) {
            if (player.isPlaying) {
                player.pause()
            }
        }
        becomingNoisyReceiver.register()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val currentSession = mediaSession
        val currentPlayer = currentSession?.player
        if (currentPlayer == null || !currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressTracker()
        becomingNoisyReceiver.unregister()
        serviceScope.cancel()

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }

    private fun startProgressTracker() {
        if (progressTrackingJob?.isActive == true) return
        progressTrackingJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    val duration = player.duration.coerceAtLeast(0L)
                    val buffered = player.bufferedPosition

                    playbackStateHolder.updateProgress(currentPos, duration, buffered)

                    // Enforce the >= 50% play count rule for Top 50 Leaderboard
                    if (duration > 0 && !hasCountedCurrentPlay && activeTrackId != null) {
                        val fraction = currentPos.toFloat() / duration.toFloat()
                        if (fraction >= Constants.PLAY_COUNT_COMPLETION_THRESHOLD) {
                            hasCountedCurrentPlay = true
                            val trackIdToRecord = activeTrackId!!
                            serviceScope.launch(Dispatchers.IO) {
                                database.playCountDao().incrementPlayCount(
                                    trackId = trackIdToRecord,
                                    timestamp = System.currentTimeMillis(),
                                    completion = fraction
                                )
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    private inner class PlayerEventListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playbackStateHolder.updatePlaying(isPlaying)
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
                playbackStateHolder.updateProgress(
                    player.currentPosition,
                    player.duration.coerceAtLeast(0L),
                    player.bufferedPosition
                )
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newTrackId = mediaItem?.mediaId?.toLongOrNull()
            if (newTrackId != activeTrackId) {
                activeTrackId = newTrackId
                hasCountedCurrentPlay = false
            }

            if (mediaItem != null) {
                serviceScope.launch(Dispatchers.IO) {
                    val trackEntity = if (newTrackId != null) database.trackDao().getTrackById(newTrackId) else null
                    val playCount = if (newTrackId != null) {
                        database.playCountDao().getPlayCountByTrackId(newTrackId)?.playCount ?: 0
                    } else 0

                    val track = trackEntity?.toDomain(playCount = playCount) ?: run {
                        val meta = mediaItem.mediaMetadata
                        com.vk.starscriptmusic.domain.model.Track(
                            id = newTrackId ?: mediaItem.mediaId.hashCode().toLong(),
                            title = meta.title?.toString() ?: "Live Stream",
                            artist = meta.artist?.toString() ?: "Radio",
                            album = meta.albumTitle?.toString() ?: "Internet Radio",
                            durationMs = 0L,
                            contentUri = mediaItem.requestMetadata.mediaUri?.toString() ?: mediaItem.mediaId,
                            albumArtUri = meta.artworkUri?.toString(),
                            dateAdded = System.currentTimeMillis(),
                            size = 0L
                        )
                    }

                    launch(Dispatchers.Main) {
                        val queue = playbackStateHolder.state.value.queue
                        val index = player.currentMediaItemIndex
                        playbackStateHolder.updateCurrentTrack(track, queue, index)
                    }
                }
            } else {
                playbackStateHolder.updateCurrentTrack(null, emptyList(), -1)
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            playbackStateHolder.updateRepeatMode(repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            playbackStateHolder.updateShuffleMode(shuffleModeEnabled)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    playbackStateHolder.updateProgress(
                        player.currentPosition,
                        player.duration.coerceAtLeast(0L),
                        player.bufferedPosition
                    )
                }
                Player.STATE_ENDED -> {
                    playbackStateHolder.updateProgress(
                        player.duration.coerceAtLeast(0L),
                        player.duration.coerceAtLeast(0L),
                        player.duration.coerceAtLeast(0L)
                    )
                }
                Player.STATE_IDLE -> {
                    // Idle state
                }
                Player.STATE_BUFFERING -> {
                    // Buffering state
                }
            }
        }
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): com.google.common.util.concurrent.ListenableFuture<MutableList<MediaItem>> {
            val updatedItems = mediaItems.map { it.buildUpon().setUri(it.requestMetadata.mediaUri).build() }.toMutableList()
            return com.google.common.util.concurrent.Futures.immediateFuture(updatedItems)
        }
    }
}
