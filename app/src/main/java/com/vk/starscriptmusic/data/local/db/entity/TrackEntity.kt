package com.vk.starscriptmusic.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vk.starscriptmusic.domain.model.Track

/**
 * Room Entity representing cached audio track metadata.
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["isFavorite"])
    ]
)
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val size: Long,
    val bitrate: Int? = null,
    val mimeType: String? = null,
    val isFavorite: Boolean = false
) {
    fun toDomain(playCount: Int = 0): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            contentUri = contentUri,
            albumArtUri = albumArtUri,
            dateAdded = dateAdded,
            size = size,
            bitrate = bitrate,
            mimeType = mimeType,
            isFavorite = isFavorite,
            playCount = playCount
        )
    }
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        contentUri = contentUri,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded,
        size = size,
        bitrate = bitrate,
        mimeType = mimeType,
        isFavorite = isFavorite
    )
}
