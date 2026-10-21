package com.vk.starscriptmusic.core.common

/**
 * Global Constants for Starscript Music.
 */
object Constants {
    const val DATABASE_NAME = "starscript_music.db"
    const val NOTIFICATION_CHANNEL_ID = "starscript_playback_channel"
    const val NOTIFICATION_ID = 1001

    // Playback constants
    const val PLAY_COUNT_COMPLETION_THRESHOLD = 0.50f // >= 50% plays required for Top 50 count
    const val DEFAULT_CROSSFADE_SECONDS = 0
    const val MAX_CROSSFADE_SECONDS = 8

    // MediaSession Actions
    const val ACTION_PLAY = "com.vk.starscriptmusic.ACTION_PLAY"
    const val ACTION_PAUSE = "com.vk.starscriptmusic.ACTION_PAUSE"
    const val ACTION_SKIP_NEXT = "com.vk.starscriptmusic.ACTION_SKIP_NEXT"
    const val ACTION_SKIP_PREV = "com.vk.starscriptmusic.ACTION_SKIP_PREV"
    const val ACTION_SEEK_TO = "com.vk.starscriptmusic.ACTION_SEEK_TO"

    // Preferences & Backup
    const val PREFERENCES_NAME = "starscript_prefs"
    const val BACKUP_FILE_NAME = "starscript_backup.json"
}
