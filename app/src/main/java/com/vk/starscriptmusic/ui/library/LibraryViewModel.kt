package com.vk.starscriptmusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.CreatePlaylistUseCase
import com.vk.starscriptmusic.domain.usecase.GetAllTracksUseCase
import com.vk.starscriptmusic.domain.usecase.GetArtistsUseCase
import com.vk.starscriptmusic.domain.usecase.GetPlaylistsUseCase
import com.vk.starscriptmusic.domain.usecase.GetTop50TracksUseCase
import com.vk.starscriptmusic.domain.usecase.ScanMediaStoreUseCase
import com.vk.starscriptmusic.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    HOME("Home"),
    SONGS("Songs"),
    PLAYLISTS("Playlists"),
    ARTISTS("Artists"),
    RADIO("Radio")
}

enum class SortOption(val displayName: String) {
    TITLE_AZ("Title (A–Z)"),
    TITLE_ZA("Title (Z–A)"),
    DATE_ADDED("Date Added (Newest)"),
    ARTIST("Artist"),
    DURATION("Duration (Longest)"),
    MOST_PLAYED("Most Played")
}

private data class DataTuple(
    val tracks: List<Track>,
    val artists: List<Artist>,
    val playlists: List<Playlist>,
    val mostPlayed: List<Track>
)

private data class FilterTuple(
    val query: String,
    val tab: LibraryTab,
    val sort: SortOption,
    val isScanning: Boolean
)

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val mostPlayedTracks: List<Track> = emptyList(),
    val rediscoverTracks: List<Track> = emptyList(),
    val forYouNowTracks: List<Track> = emptyList(),
    val recentTracks: List<Track> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.HOME,
    val sortOption: SortOption = SortOption.TITLE_AZ,
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllTracksUseCase: GetAllTracksUseCase,
    private val getArtistsUseCase: GetArtistsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getTop50TracksUseCase: GetTop50TracksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val scanMediaStoreUseCase: ScanMediaStoreUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val musicRepository: MusicRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(LibraryTab.HOME)
    private val _sortOption = MutableStateFlow(SortOption.TITLE_AZ)
    private val _isScanning = MutableStateFlow(false)

    private val libraryDataFlow = combine(
        getAllTracksUseCase(),
        getArtistsUseCase(),
        getPlaylistsUseCase(),
        getTop50TracksUseCase()
    ) { tracks, artists, playlists, mostPlayed ->
        DataTuple(tracks, artists, playlists, mostPlayed)
    }

    private val filterStateFlow = combine(
        _searchQuery,
        _selectedTab,
        _sortOption,
        _isScanning
    ) { query, tab, sort, isScanning ->
        FilterTuple(query, tab, sort, isScanning)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        libraryDataFlow,
        filterStateFlow,
        playbackRepository.playbackState
    ) { data, filter, playbackState ->
        val query = filter.query
        val sort = filter.sort

        // Filter and sort tracks
        val filteredTracks = if (query.isBlank()) {
            data.tracks
        } else {
            data.tracks.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }

        val sortedTracks = when (sort) {
            SortOption.TITLE_AZ -> filteredTracks.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> filteredTracks.sortedByDescending { it.title.lowercase() }
            SortOption.DATE_ADDED -> filteredTracks.sortedByDescending { it.dateAdded }
            SortOption.ARTIST -> filteredTracks.sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
            SortOption.DURATION -> filteredTracks.sortedByDescending { it.durationMs }
            SortOption.MOST_PLAYED -> filteredTracks.sortedByDescending { it.playCount }
        }

        val filteredArtists = if (query.isBlank()) {
            data.artists
        } else {
            data.artists.filter { it.name.contains(query, ignoreCase = true) }
        }

        val filteredPlaylists = if (query.isBlank()) {
            data.playlists
        } else {
            data.playlists.filter { it.name.contains(query, ignoreCase = true) }
        }

        val filteredMostPlayed = if (query.isBlank()) {
            data.mostPlayed
        } else {
            data.mostPlayed.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }
        }

        // Personalized smart sets
        val rediscover = data.tracks.filter { it.playCount <= 1 }.shuffled().take(15)
        val forYouNow = (data.mostPlayed.take(10) + data.tracks.filter { it.isFavorite }).distinctBy { it.id }.take(15)
        val recents = data.tracks.sortedByDescending { it.dateAdded }.take(20)

        LibraryUiState(
            tracks = sortedTracks,
            artists = filteredArtists,
            playlists = filteredPlaylists,
            mostPlayedTracks = filteredMostPlayed,
            rediscoverTracks = rediscover,
            forYouNowTracks = forYouNow,
            recentTracks = recents,
            selectedTab = filter.tab,
            sortOption = sort,
            searchQuery = query,
            isScanning = filter.isScanning,
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

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
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

    fun playFavorites() {
        val favs = uiState.value.tracks.filter { it.isFavorite }
        if (favs.isNotEmpty()) {
            playAll(favs)
        } else if (uiState.value.tracks.isNotEmpty()) {
            playAll(uiState.value.tracks)
        }
    }

    fun playHistory() {
        val recents = uiState.value.recentTracks
        if (recents.isNotEmpty()) {
            playAll(recents)
        }
    }

    fun playRediscover() {
        val redis = uiState.value.rediscoverTracks
        if (redis.isNotEmpty()) {
            playAll(redis)
        }
    }

    fun playForYouNow() {
        val forYou = uiState.value.forYouNowTracks
        if (forYou.isNotEmpty()) {
            playAll(forYou)
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

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            musicRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }
}
