package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve all songs by a given artist or band.
 */
class GetArtistTracksUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(artist: String): Flow<List<Track>> {
        return musicRepository.getTracksByArtist(artist)
    }
}
