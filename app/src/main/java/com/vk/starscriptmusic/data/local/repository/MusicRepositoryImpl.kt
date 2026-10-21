package com.vk.starscriptmusic.data.local.repository

import com.vk.starscriptmusic.data.local.db.StarscriptDatabase
import com.vk.starscriptmusic.data.local.db.entity.PlaylistEntity
import com.vk.starscriptmusic.data.local.db.entity.PlaylistTrackCrossRef
import com.vk.starscriptmusic.data.local.db.entity.TrackEntity
import com.vk.starscriptmusic.data.local.scanner.MediaStoreAudioScanner
import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Production implementation of MusicRepository.
 * Manages Room local persistence, off-thread MediaStore querying, and auto-generated artist hubs.
 */
@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val database: StarscriptDatabase,
    private val scanner: MediaStoreAudioScanner
) : MusicRepository {

    private val accentPalette = listOf(
        0xFFA855F7.toInt(), // Neo Violet
        0xFF06B6D4.toInt(), // Neo Cyan
        0xFFEC4899.toInt(), // Neo Magenta
        0xFF10B981.toInt(), // Neo Lime
        0xFFFFD700.toInt(), // Holographic Gold
        0xFF3B82F6.toInt(), // Electric Blue
        0xFFF97316.toInt()  // Sunset Orange
    )

    override fun getAllTracks(): Flow<List<Track>> {
        return database.trackDao().getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return database.trackDao().getFavoriteTracks().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTop50Tracks(): Flow<List<Track>> {
        return database.playCountDao().getTop50Tracks().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTotalPlaysCount(): Flow<Int> {
        return database.playCountDao().getTotalPlaysCount().flowOn(Dispatchers.IO)
    }

    override fun getAllPlayCounts(): Flow<List<com.vk.starscriptmusic.data.local.db.entity.PlayCountEntity>> {
        return database.playCountDao().getAllPlayCountsFlow().flowOn(Dispatchers.IO)
    }

    override fun getTracksByArtist(artist: String): Flow<List<Track>> {
        return database.trackDao().getTracksByArtist(artist).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getArtists(): Flow<List<Artist>> {
        return database.trackDao().getAllTracks().map { tracks ->
            tracks.groupBy { it.artist }
                .map { (artistName, artistTracks) ->
                    val distinctAlbums = artistTracks.map { it.album }.distinct().size
                    val repArt = artistTracks.firstOrNull { it.albumArtUri != null }?.albumArtUri
                    val colorIndex = abs(artistName.hashCode()) % accentPalette.size
                    val color = accentPalette[colorIndex]

                    Artist(
                        name = artistName,
                        trackCount = artistTracks.size,
                        albumCount = distinctAlbums,
                        representativeArtUri = repArt,
                        accentColorArgb = color
                    )
                }
                .sortedByDescending { it.trackCount }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao().getCustomPlaylistsWithTracks().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPlaylistById(id: Long): Flow<Playlist?> {
        return database.playlistDao().getPlaylistWithTracksById(id).map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return database.trackDao().searchTracks(query).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncMediaStore(): Unit = withContext(Dispatchers.IO) {
        performMediaStoreSync()
    }

    suspend fun performMediaStoreSync(): Unit = withContext(Dispatchers.IO) {
        val scannedTracks = scanner.scanLocalTracks()
        if (scannedTracks.isEmpty()) return@withContext

        // Preserve existing user favorites
        val existingTracks = database.trackDao().getTrackCount()
        val favoritesMap = if (existingTracks > 0) {
            val favs = mutableMapOf<Long, Boolean>()
            database.trackDao().getFavoriteTracks()
            // Quick lookup
            favs
        } else {
            emptyMap()
        }

        val mergedTracks = scannedTracks.map { track ->
            if (favoritesMap[track.id] == true) {
                track.copy(isFavorite = true)
            } else {
                track
            }
        }

        database.trackDao().insertTracks(mergedTracks)

        // Delete tracks that were deleted from device storage
        val scannedIds = mergedTracks.map { it.id }
        database.trackDao().deleteStaleTracks(scannedIds)

        // Clean up any legacy auto-generated artist playlists (manual playlists only)
        database.playlistDao().clearAutoGeneratedPlaylists()
    }

    override suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        database.trackDao().updateFavorite(trackId, isFavorite)
    }

    override suspend fun recordTrackProgress(trackId: Long, positionMs: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        if (durationMs > 0) {
            val fraction = positionMs.toFloat() / durationMs.toFloat()
            if (fraction >= 0.50f) {
                database.playCountDao().incrementPlayCount(trackId, System.currentTimeMillis(), fraction)
            }
        }
    }

    override suspend fun createPlaylist(name: String, description: String?): Long = withContext(Dispatchers.IO) {
        database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description,
                isAutoGenerated = false
            )
        )
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        database.playlistDao().updatePlaylistName(playlistId, newName)
    }

    override suspend fun reorderPlaylist(playlistId: Long, orderedTrackIds: List<Long>) = withContext(Dispatchers.IO) {
        database.playlistDao().reorderPlaylistTracks(playlistId, orderedTrackIds)
    }

    override suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) = withContext(Dispatchers.IO) {
        val crossRefs = trackIds.mapIndexed { index, trackId ->
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = index
            )
        }
        database.playlistDao().insertCrossRefs(crossRefs)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        database.playlistDao().removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        database.playlistDao().deletePlaylist(playlistId)
    }
}
