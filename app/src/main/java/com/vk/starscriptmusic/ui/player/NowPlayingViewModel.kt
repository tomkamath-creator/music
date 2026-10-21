package com.vk.starscriptmusic.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.vk.starscriptmusic.core.util.DynamicPaletteColors
import com.vk.starscriptmusic.core.util.PaletteExtractor
import com.vk.starscriptmusic.data.network.SongIntel
import com.vk.starscriptmusic.data.network.SongIntelService
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val playbackState: PlaybackState = PlaybackState(),
    val palette: DynamicPaletteColors = DynamicPaletteColors(),
    val isFavorite: Boolean = false,
    val songIntel: SongIntel? = null,
    val isIntelLoading: Boolean = false
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackRepository: PlaybackRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val songIntelService: SongIntelService
) : ViewModel() {

    private val paletteColors = MutableStateFlow(DynamicPaletteColors())
    private val songIntelFlow = MutableStateFlow<SongIntel?>(null)
    private val isIntelLoadingFlow = MutableStateFlow(false)

    val uiState: StateFlow<NowPlayingUiState> = combine(
        playbackRepository.playbackState,
        paletteColors,
        songIntelFlow,
        isIntelLoadingFlow
    ) { state, palette, intel, isLoading ->
        NowPlayingUiState(
            playbackState = state,
            palette = palette,
            isFavorite = state.currentTrack?.isFavorite ?: false,
            songIntel = intel,
            isIntelLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NowPlayingUiState()
    )

    init {
        // Automatically extract dynamic palette & fetch Song Intel whenever the active track changes
        viewModelScope.launch {
            playbackRepository.playbackState
                .map { it.currentTrack }
                .distinctUntilChanged { old, new -> old?.id == new?.id && old?.contentUri == new?.contentUri }
                .collect { track ->
                    if (track != null) {
                        // Palette extraction
                        val initialArtUri = track.albumArtUri
                        val initialColors = PaletteExtractor.extractColors(context, initialArtUri)
                        paletteColors.value = initialColors

                        // Fetch Song Intel & high-res replacement art
                        isIntelLoadingFlow.value = true
                        try {
                            val intel = songIntelService.getSongIntel(
                                title = track.title,
                                artist = track.artist,
                                album = track.album
                            )
                            songIntelFlow.value = intel

                            // If track was missing album art and iTunes returned 1400x1400 high-res art, sample colors from it
                            if (initialArtUri == null && intel.highResArtworkUrl != null) {
                                val highResColors = PaletteExtractor.extractColors(context, intel.highResArtworkUrl)
                                paletteColors.value = highResColors
                            }
                        } catch (_: Exception) {
                            // Fallback state
                        } finally {
                            isIntelLoadingFlow.value = false
                        }
                    } else {
                        songIntelFlow.value = null
                    }
                }
        }
    }

    fun playPause() {
        playbackRepository.playPause()
    }

    fun skipNext() {
        playbackRepository.skipNext()
    }

    fun skipPrevious() {
        playbackRepository.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }

    fun seekRelative(offsetMs: Long) {
        playbackRepository.seekRelative(offsetMs)
    }

    fun toggleRepeat() {
        val currentMode = uiState.value.playbackState.repeatMode
        val nextMode = when (currentMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        playbackRepository.setRepeatMode(nextMode)
    }

    fun toggleShuffle() {
        val current = uiState.value.playbackState.shuffleModeEnabled
        playbackRepository.setShuffleMode(!current)
    }

    fun toggleFavorite() {
        val track = uiState.value.playbackState.currentTrack ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(track.id, !track.isFavorite)
        }
    }
}
