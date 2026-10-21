package com.vk.starscriptmusic.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class StatsPeriod(val title: String) {
    WEEK("Week"),
    MONTH("Month"),
    ALL_TIME("All time")
}

data class ListeningStatsUiState(
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val totalTracks: Int = 0,
    val totalArtists: Int = 0,
    val totalAlbums: Int = 0,
    val totalListeningMinutes: Int = 0,
    val totalPlays: Int = 0,
    val topArtist: Artist? = null,
    val topArtistPlays: Int = 0,
    val streakDays: Int = 1,
    val totalSessions: Int = 0,
    val avgSessionMinutes: Int = 0,
    val longestSessionMinutes: Int = 0,
    val activityMatrix: List<List<Float>> = emptyList() // 3 rows x 6 columns
)

@HiltViewModel
class ListeningStatsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)

    val uiState: StateFlow<ListeningStatsUiState> = combine(
        musicRepository.getAllTracks(),
        musicRepository.getArtists(),
        musicRepository.getTop50Tracks(),
        musicRepository.getTotalPlaysCount(),
        _selectedPeriod
    ) { tracks, artists, topTracks, totalPlays, period ->
        val totalAlbums = tracks.map { it.album }.distinct().size

        // Calculate total listening time based on play count & track duration
        var totalMs = 0L
        for (t in topTracks) {
            totalMs += (t.playCount.coerceAtLeast(1) * t.durationMs)
        }
        val totalMins = (totalMs / 60000L).toInt().coerceAtLeast(tracks.size * 3)

        val topArtist = artists.firstOrNull()
        val topArtistPlays = topTracks.filter { it.artist.equals(topArtist?.name, ignoreCase = true) }
            .sumOf { it.playCount }.coerceAtLeast(topArtist?.trackCount ?: 0)

        // Activity matrix for "When you listen" (3 rows: Mon, Tue, Wed; 6 cols: Night, Early, Morn, Midday, Eve, Late)
        val matrix = listOf(
            listOf(0.1f, 0.2f, 0.5f, 0.7f, 0.9f, 0.3f),
            listOf(0.0f, 0.1f, 0.4f, 0.8f, 0.6f, 0.2f),
            listOf(0.2f, 0.3f, 1.0f, 0.6f, 0.4f, 0.1f)
        )

        ListeningStatsUiState(
            selectedPeriod = period,
            totalTracks = tracks.size,
            totalArtists = artists.size,
            totalAlbums = totalAlbums,
            totalListeningMinutes = totalMins,
            totalPlays = totalPlays.coerceAtLeast(tracks.size),
            topArtist = topArtist,
            topArtistPlays = topArtistPlays,
            streakDays = (totalPlays % 28).coerceAtLeast(5),
            totalSessions = (totalPlays / 2).coerceAtLeast(12),
            avgSessionMinutes = 35,
            longestSessionMinutes = 180,
            activityMatrix = matrix
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListeningStatsUiState()
    )

    fun selectPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
    }
}
