package com.vk.starscriptmusic.ui.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.GetArtistTracksUseCase
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistHubUiState(
    val artistName: String = "",
    val tracks: List<Track> = emptyList(),
    val totalDurationMs: Long = 0L,
    val albumCount: Int = 0,
    val heroArtUri: String? = null,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class ArtistHubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArtistTracksUseCase: GetArtistTracksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val artistName: String = checkNotNull(savedStateHandle["artistName"])

    val uiState: StateFlow<ArtistHubUiState> = combine(
        getArtistTracksUseCase(artistName),
        playbackRepository.playbackState
    ) { tracks, playbackState ->
        val distinctAlbums = tracks.map { it.album }.distinct().size
        val heroArt = tracks.firstOrNull { it.albumArtUri != null }?.albumArtUri
        val totalMs = tracks.sumOf { it.durationMs }

        ArtistHubUiState(
            artistName = artistName,
            tracks = tracks,
            totalDurationMs = totalMs,
            albumCount = distinctAlbums,
            heroArtUri = heroArt,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtistHubUiState(artistName = artistName)
    )

    fun playTrack(track: Track) {
        val tracks = uiState.value.tracks
        playbackRepository.playTrack(track, tracks)
    }

    fun playAll() {
        val tracks = uiState.value.tracks
        if (tracks.isNotEmpty()) {
            playbackRepository.playTrack(tracks.first(), tracks)
        }
    }

    fun shuffleAll() {
        val tracks = uiState.value.tracks
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
}
