package com.vk.starscriptmusic.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        musicRepository.getPlaylistById(playlistId),
        playbackRepository.playbackState
    ) { playlist, playbackState ->
        PlaylistDetailUiState(
            playlist = playlist,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistDetailUiState()
    )

    fun playTrack(track: Track) {
        val tracks = uiState.value.playlist?.tracks ?: emptyList()
        playbackRepository.playTrack(track, tracks)
    }

    fun playAll() {
        val tracks = uiState.value.playlist?.tracks ?: emptyList()
        if (tracks.isNotEmpty()) {
            playbackRepository.playTrack(tracks.first(), tracks)
        }
    }

    fun shuffleAll() {
        val tracks = uiState.value.playlist?.tracks ?: emptyList()
        if (tracks.isNotEmpty()) {
            val shuffled = tracks.shuffled()
            playbackRepository.setShuffleMode(true)
            playbackRepository.playTrack(shuffled.first(), shuffled)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            toggleFavoriteUseCase(track.id, !track.isFavorite)
        }
    }

    fun removeTrack(trackId: Long) {
        viewModelScope.launch {
            musicRepository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }
}
