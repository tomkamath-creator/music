package com.vk.starscriptmusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.CreatePlaylistUseCase
import com.vk.starscriptmusic.domain.usecase.GetAllTracksUseCase
import com.vk.starscriptmusic.domain.usecase.GetArtistsUseCase
import com.vk.starscriptmusic.domain.usecase.GetPlaylistsUseCase
import com.vk.starscriptmusic.domain.usecase.ScanMediaStoreUseCase
import com.vk.starscriptmusic.domain.usecase.SearchTracksUseCase
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab {
    SONGS,
    ARTISTS,
    PLAYLISTS,
    FAVORITES
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<Track> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllTracksUseCase: GetAllTracksUseCase,
    private val getArtistsUseCase: GetArtistsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val searchTracksUseCase: SearchTracksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val scanMediaStoreUseCase: ScanMediaStoreUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    private val _isScanning = MutableStateFlow(false)

    private val libraryDataFlow = combine(
        getAllTracksUseCase(),
        getArtistsUseCase(),
        getPlaylistsUseCase()
    ) { tracks, artists, playlists ->
        Triple(tracks, artists, playlists)
    }

    private val filterStateFlow = combine(
        _searchQuery,
        _selectedTab,
        _isScanning
    ) { query, tab, isScanning ->
        Triple(query, tab, isScanning)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        libraryDataFlow,
        filterStateFlow,
        playbackRepository.playbackState
    ) { (tracks, artists, playlists), (query, tab, isScanning), playbackState ->
        val filteredTracks = if (query.isBlank()) {
            tracks
        } else {
            tracks.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }

        val filteredArtists = if (query.isBlank()) {
            artists
        } else {
            artists.filter { it.name.contains(query, ignoreCase = true) }
        }

        LibraryUiState(
            tracks = filteredTracks,
            artists = filteredArtists,
            playlists = playlists,
            favorites = tracks.filter { it.isFavorite },
            selectedTab = tab,
            searchQuery = query,
            isScanning = isScanning,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    init {
        syncLibrary()
    }

    fun syncLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                scanMediaStoreUseCase()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: Track, queue: List<Track>) {
        playbackRepository.playTrack(track, queue)
    }

    fun playAll(tracks: List<Track>) {
        if (tracks.isNotEmpty()) {
            playbackRepository.playTrack(tracks.first(), tracks)
        }
    }

    fun shuffleAll(tracks: List<Track>) {
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

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            createPlaylistUseCase(name, description)
        }
    }
}
