package com.vk.starscriptmusic.data.media

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import com.vk.starscriptmusic.MainActivity
import com.vk.starscriptmusic.R
import com.vk.starscriptmusic.core.common.Constants

/**
 * Configures the system media notification provider for Android 14+ lock screen controls.
 */
class MediaNotificationManager(private val context: Context) {

    fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun createNotificationProvider(): DefaultMediaNotificationProvider {
        return DefaultMediaNotificationProvider.Builder(context)
            .setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.notification_channel_name)
            .setNotificationId(Constants.NOTIFICATION_ID)
            .build()
    }
}
