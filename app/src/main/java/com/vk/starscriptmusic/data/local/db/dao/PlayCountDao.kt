package com.vk.starscriptmusic.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vk.starscriptmusic.data.local.db.entity.PlayCountEntity
import com.vk.starscriptmusic.data.local.db.entity.TrackWithPlayCount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for managing track play counts and generating the Top 50 Leaderboard.
 */
@Dao
interface PlayCountDao {

    @Transaction
    @Query("""
        SELECT t.*, COALESCE(p.playCount, 0) AS playCount
        FROM tracks t
        INNER JOIN play_counts p ON t.id = p.trackId
        WHERE p.playCount > 0
        ORDER BY p.playCount DESC, p.lastPlayedTimestamp DESC
        LIMIT 50
    """)
    fun getTop50Tracks(): Flow<List<TrackWithPlayCount>>

    @Query("SELECT * FROM play_counts WHERE trackId = :trackId LIMIT 1")
    suspend fun getPlayCountByTrackId(trackId: Long): PlayCountEntity?

    @Query("SELECT * FROM play_counts WHERE trackId = :trackId LIMIT 1")
    fun getPlayCountFlow(trackId: Long): Flow<PlayCountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayCount(entity: PlayCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PlayCountEntity>)

    @Query("""
        INSERT INTO play_counts (trackId, playCount, lastPlayedTimestamp, completionPercentage)
        VALUES (:trackId, 1, :timestamp, :completion)
        ON CONFLICT(trackId) DO UPDATE SET
            playCount = play_counts.playCount + 1,
            lastPlayedTimestamp = :timestamp,
            completionPercentage = :completion
    """)
    suspend fun incrementPlayCount(trackId: Long, timestamp: Long, completion: Float)

    @Query("SELECT * FROM play_counts")
    suspend fun getAllPlayCounts(): List<PlayCountEntity>

    @Query("DELETE FROM play_counts WHERE trackId = :trackId")
    suspend fun deletePlayCountByTrackId(trackId: Long)

    @Query("DELETE FROM play_counts")
    suspend fun clearAllPlayCounts()
}
