package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to search songs by title, artist, or album.
 */
class SearchTracksUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(query: String): Flow<List<Track>> {
        return musicRepository.searchTracks(query)
    }
}
