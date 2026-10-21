package com.vk.starscriptmusic.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity tracking playback frequency for the Top 50 Leaderboard ("Hall of Fame").
 * Strictly updated when the user listens to >= 50% of a track.
 */
@Entity(
    tableName = "play_counts",
    indices = [
        Index(value = ["playCount"]),
        Index(value = ["lastPlayedTimestamp"])
    ]
)
data class PlayCountEntity(
    @PrimaryKey
    val trackId: Long,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val completionPercentage: Float = 0f
)
