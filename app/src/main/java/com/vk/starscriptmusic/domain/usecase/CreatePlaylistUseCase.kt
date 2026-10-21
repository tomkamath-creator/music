package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case to create a new user playlist.
 */
class CreatePlaylistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Long {
        return musicRepository.createPlaylist(name, description)
    }
}
