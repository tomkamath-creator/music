package com.vk.starscriptmusic.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.vk.starscriptmusic.core.util.DynamicPaletteColors
import com.vk.starscriptmusic.core.util.PaletteExtractor
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
    val isFavorite: Boolean = false
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackRepository: PlaybackRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val paletteColors = MutableStateFlow(DynamicPaletteColors())

    val uiState: StateFlow<NowPlayingUiState> = combine(
        playbackRepository.playbackState,
        paletteColors
    ) { state, palette ->
        NowPlayingUiState(
            playbackState = state,
            palette = palette,
            isFavorite = state.currentTrack?.isFavorite ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NowPlayingUiState()
    )

    init {
        // Automatically extract dynamic palette whenever the active track changes
        viewModelScope.launch {
            playbackRepository.playbackState
                .map { it.currentTrack?.albumArtUri }
                .distinctUntilChanged()
                .collect { artUri ->
                    val colors = PaletteExtractor.extractColors(context, artUri)
                    paletteColors.value = colors
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
