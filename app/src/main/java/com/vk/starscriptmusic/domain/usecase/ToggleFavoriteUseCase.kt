package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case to toggle the favorite state of a track.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(trackId: Long, isFavorite: Boolean) {
        musicRepository.toggleFavorite(trackId, isFavorite)
    }
}
