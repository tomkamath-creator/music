package com.vk.starscriptmusic.data.local.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.vk.starscriptmusic.data.local.db.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-performance off-main-thread scanner querying local audio files via MediaStore.Audio.
 * Extracts ID3 metadata, content URIs, and album art pointers for mp3, flac, wav, m4a, and ogg formats.
 */
@Singleton
class MediaStoreAudioScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val albumArtBaseUri: Uri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanLocalTracks(): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()
        val contentResolver: ContentResolver = context.contentResolver

        val collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // Filter: Must be music and longer than 10 seconds (excludes ringtones, notification blips)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)?.trim().takeUnless { it.isNullOrBlank() }
                        ?: "Unknown Title"

                    val rawArtist = cursor.getString(artistColumn)?.trim()
                    val artist = if (rawArtist.isNullOrBlank() || rawArtist.equals("<unknown>", ignoreCase = true)) {
                        "Unknown Artist"
                    } else {
                        rawArtist
                    }

                    val rawAlbum = cursor.getString(albumColumn)?.trim()
                    val album = if (rawAlbum.isNullOrBlank() || rawAlbum.equals("<unknown>", ignoreCase = true)) {
                        "Unknown Album"
                    } else {
                        rawAlbum
                    }

                    val albumId = cursor.getLong(albumIdColumn)
                    val durationMs = cursor.getLong(durationColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()
                    } else {
                        null
                    }

                    tracks.add(
                        TrackEntity(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = durationMs,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            dateAdded = dateAdded,
                            size = size,
                            mimeType = mimeType,
                            isFavorite = false
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Error safety fallback
        }

        tracks
    }
}
