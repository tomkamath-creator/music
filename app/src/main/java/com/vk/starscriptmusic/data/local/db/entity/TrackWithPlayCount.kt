package com.vk.starscriptmusic.data.local.db.entity

import androidx.room.Embedded
import com.vk.starscriptmusic.domain.model.Track

/**
 * Result row combining TrackEntity metadata with its verified play count.
 */
data class TrackWithPlayCount(
    @Embedded
    val track: TrackEntity,
    val playCount: Int
) {
    fun toDomain(): Track {
        return track.toDomain(playCount = playCount)
    }
}
