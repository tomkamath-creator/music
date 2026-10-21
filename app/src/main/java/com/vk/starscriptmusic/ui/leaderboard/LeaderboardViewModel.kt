package com.vk.starscriptmusic.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.GetTop50TracksUseCase
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val topTracks: List<Track> = emptyList(),
    val totalPlaysLogged: Int = 0,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getTop50TracksUseCase: GetTop50TracksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val uiState: StateFlow<LeaderboardUiState> = combine(
        getTop50TracksUseCase(),
        playbackRepository.playbackState
    ) { tracks, playbackState ->
        val totalPlays = tracks.sumOf { it.playCount }
        LeaderboardUiState(
            topTracks = tracks,
            totalPlaysLogged = totalPlays,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderboardUiState()
    )

    fun playTrack(track: Track) {
        val tracks = uiState.value.topTracks
        playbackRepository.playTrack(track, tracks)
    }

    fun playAll() {
        val tracks = uiState.value.topTracks
        if (tracks.isNotEmpty()) {
            playbackRepository.playTrack(tracks.first(), tracks)
        }
    }

    fun shuffleAll() {
        val tracks = uiState.value.topTracks
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
