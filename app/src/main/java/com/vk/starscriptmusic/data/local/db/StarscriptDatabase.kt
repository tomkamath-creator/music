package com.vk.starscriptmusic.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vk.starscriptmusic.data.local.db.converters.Converters
import com.vk.starscriptmusic.data.local.db.dao.PlayCountDao
import com.vk.starscriptmusic.data.local.db.dao.PlaylistDao
import com.vk.starscriptmusic.data.local.db.dao.TrackDao
import com.vk.starscriptmusic.data.local.db.entity.PlayCountEntity
import com.vk.starscriptmusic.data.local.db.entity.PlaylistEntity
import com.vk.starscriptmusic.data.local.db.entity.PlaylistTrackCrossRef
import com.vk.starscriptmusic.data.local.db.entity.TrackEntity

/**
 * Main Room Database for Starscript Music.
 * Persists local tracks metadata, playback frequency metrics, and user/artist playlists.
 */
@Database(
    entities = [
        TrackEntity::class,
        PlayCountEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StarscriptDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun playCountDao(): PlayCountDao

    abstract fun playlistDao(): PlaylistDao
}
