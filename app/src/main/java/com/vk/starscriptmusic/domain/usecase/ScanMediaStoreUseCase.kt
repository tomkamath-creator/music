package com.vk.starscriptmusic.domain.usecase

import com.vk.starscriptmusic.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case to scan device storage for new, modified, or deleted audio tracks and auto-generate artist hubs.
 */
class ScanMediaStoreUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke() {
        musicRepository.syncMediaStore()
    }
}
