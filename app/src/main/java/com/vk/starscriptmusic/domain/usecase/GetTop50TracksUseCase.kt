package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve the Top 50 most played tracks ("Hall of Fame")
 * based on verified >= 50% playback completions.
 */
class GetTop50TracksUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Track>> {
        return musicRepository.getTop50Tracks()
    }
}
