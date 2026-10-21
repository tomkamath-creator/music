package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve auto-grouped artists with album counts, track counts, and glowing accent colors.
 */
class GetArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return musicRepository.getArtists()
    }
}
