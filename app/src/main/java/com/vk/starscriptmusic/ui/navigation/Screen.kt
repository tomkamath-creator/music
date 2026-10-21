package com.vk.starscriptmusic.ui.navigation

import android.net.Uri

/**
 * Type-safe navigation routes for Starscript Music.
 */
sealed class Screen(val route: String) {
    data object Library : Screen("library")

    data object Leaderboard : Screen("leaderboard")

    data object Settings : Screen("settings")

    data object ListeningStats : Screen("listening_stats")

    data object ArtistHub : Screen("artist_hub/{artistName}") {
        fun createRoute(artistName: String): String {
            return "artist_hub/${Uri.encode(artistName)}"
        }
    }

    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: Long): String {
            return "playlist_detail/$playlistId"
        }
    }
}
