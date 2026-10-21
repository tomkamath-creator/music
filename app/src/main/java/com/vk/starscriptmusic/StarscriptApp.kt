package com.vk.starscriptmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.vk.starscriptmusic.core.common.Constants
import dagger.hilt.android.HiltAndroidApp

/**
 * Starscript Music - Main Application Class
 * Initializes Hilt dependency injection and system playback notification channels.
 */
@HiltAndroidApp
class StarscriptApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createPlaybackNotificationChannel()
    }

    private fun createPlaybackNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
