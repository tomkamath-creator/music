package com.vk.starscriptmusic.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vk.starscriptmusic.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cached track metadata.
 */
@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title COLLATE NOCASE ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    fun getTrackByIdFlow(id: Long): Flow<TrackEntity?>

    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT artist FROM tracks WHERE artist IS NOT NULL AND TRIM(artist) != '' ORDER BY artist COLLATE NOCASE ASC")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)

    @Query("DELETE FROM tracks WHERE id NOT IN (:validIds)")
    suspend fun deleteStaleTracks(validIds: List<Long>)

    @Query("DELETE FROM tracks")
    suspend fun clearAllTracks()
}
