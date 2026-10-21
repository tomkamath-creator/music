package com.vk.starscriptmusic.data.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

/**
 * Headphone intelligence receiver.
 * Automatically pauses playback when headphones or external speakers are disconnected.
 */
class AudioBecomingNoisyReceiver(
    private val context: Context,
    private val onBecomingNoisy: () -> Unit
) : BroadcastReceiver() {

    private var isRegistered = false

    fun register() {
        if (!isRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            context.registerReceiver(this, filter)
            isRegistered = true
        }
    }

    fun unregister() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
            } catch (_: IllegalArgumentException) {
                // Ignore if already unregistered
            }
            isRegistered = false
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }
}
