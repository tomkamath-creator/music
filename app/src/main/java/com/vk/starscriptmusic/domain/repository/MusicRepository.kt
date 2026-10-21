package com.vk.starscriptmusic.domain.repository

import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Clean Architecture Repository Interface for audio media, playlists, and analytics.
 */
interface MusicRepository {

    fun getAllTracks(): Flow<List<Track>>

    fun getFavoriteTracks(): Flow<List<Track>>

    fun getTop50Tracks(): Flow<List<Track>>

    fun getTracksByArtist(artist: String): Flow<List<Track>>

    fun getArtists(): Flow<List<Artist>>

    fun getPlaylists(): Flow<List<Playlist>>

    fun getPlaylistById(id: Long): Flow<Playlist?>

    fun searchTracks(query: String): Flow<List<Track>>

    suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean)

    suspend fun recordTrackProgress(trackId: Long, positionMs: Long, durationMs: Long)

    suspend fun createPlaylist(name: String, description: String?): Long

    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>)

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    suspend fun deletePlaylist(playlistId: Long)

    suspend fun syncMediaStore()
}
