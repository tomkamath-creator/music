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
    val allLibraryTracks: List<Track> = emptyList(),
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        musicRepository.getPlaylistById(playlistId),
        musicRepository.getAllTracks(),
        playbackRepository.playbackState
    ) { playlist, allTracks, playbackState ->
        PlaylistDetailUiState(
            playlist = playlist,
            allLibraryTracks = allTracks,
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

    fun renamePlaylist(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            musicRepository.renamePlaylist(playlistId, newName.trim())
        }
    }

    fun deletePlaylist(onComplete: () -> Unit) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
            onComplete()
        }
    }

    fun addTracks(trackIds: List<Long>) {
        if (trackIds.isEmpty()) return
        viewModelScope.launch {
            musicRepository.addTracksToPlaylist(playlistId, trackIds)
        }
    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        val currentTracks = uiState.value.playlist?.tracks?.toMutableList() ?: return
        if (fromIndex in currentTracks.indices && toIndex in currentTracks.indices) {
            val moved = currentTracks.removeAt(fromIndex)
            currentTracks.add(toIndex, moved)
            val orderedIds = currentTracks.map { it.id }
            viewModelScope.launch {
                musicRepository.reorderPlaylist(playlistId, orderedIds)
            }
        }
    }
}
