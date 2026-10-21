package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve all cached audio tracks.
 */
class GetAllTracksUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Track>> {
        return musicRepository.getAllTracks()
    }
}
