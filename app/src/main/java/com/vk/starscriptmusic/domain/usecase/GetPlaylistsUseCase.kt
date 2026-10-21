package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve all custom and auto-generated playlists.
 */
class GetPlaylistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return musicRepository.getPlaylists()
    }
}
