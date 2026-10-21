package com.vk.starscriptmusic.ui.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.data.network.RadioBrowserService
import com.vk.starscriptmusic.data.network.RadioStation
import com.vk.starscriptmusic.domain.model.PlaybackState
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val selectedGenre: String = "Top Hits",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val playbackState: PlaybackState = PlaybackState()
)

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val radioBrowserService: RadioBrowserService,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val genres = listOf(
        "Top Hits",
        "Chill",
        "Electronic",
        "Rock",
        "Pop",
        "Jazz",
        "Classical",
        "Hip Hop",
        "Ambient",
        "Lofi",
        "Country"
    )

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    private val _selectedGenre = MutableStateFlow("Top Hits")
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<RadioUiState> = combine(
        _stations,
        _selectedGenre,
        _searchQuery,
        _isLoading,
        playbackRepository.playbackState
    ) { stations, genre, query, isLoading, playbackState ->
        RadioUiState(
            stations = stations,
            selectedGenre = genre,
            searchQuery = query,
            isLoading = isLoading,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RadioUiState()
    )

    init {
        loadStationsForGenre("Top Hits")
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
        _searchQuery.value = ""
        loadStationsForGenre(genre)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadStationsForGenre(_selectedGenre.value)
        } else {
            searchStations(query)
        }
    }

    private fun loadStationsForGenre(genre: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = if (genre == "Top Hits") {
                    radioBrowserService.getTopStations(limit = 40)
                } else {
                    radioBrowserService.getStationsByGenre(genre, limit = 35)
                }
                _stations.value = list
            } catch (_: Exception) {
                _stations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun searchStations(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = radioBrowserService.searchStations(query, limit = 35)
                _stations.value = list
            } catch (_: Exception) {
                _stations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playStation(station: RadioStation) {
        val track = station.toTrack()
        playbackRepository.playTrack(track, listOf(track))
    }
}
