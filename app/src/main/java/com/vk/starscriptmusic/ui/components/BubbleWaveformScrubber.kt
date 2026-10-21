package com.vk.starscriptmusic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.WaveformBubbleInactive
import kotlin.math.sin

/**
 * Animated Bubble Waveform Scrubber.
 * Pillowy, bouncy waveform bars that react dynamically to audio playback and touch scrubbing.
 */
@Composable
fun BubbleWaveformScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    activeColor: Color = NeoCyan,
    inactiveColor: Color = WaveformBubbleInactive
) {
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val displayFraction = if (isDragging) dragFraction else currentFraction

    val infiniteTransition = rememberInfiniteTransition(label = "bubble_bounce")
    val waveBouncePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bounce_phase"
    )

    // Pre-calculate organic wave amplitudes
    val baseAmplitudes = remember(barCount) {
        FloatArray(barCount) { index ->
            val angle = (index.toFloat() / barCount.toFloat()) * 3.14159f * 2.5f
            val raw = (sin(angle.toDouble()) * 0.45f + 0.55f).toFloat()
            raw.coerceIn(0.25f, 1.0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (size.width > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSeekTo((fraction * durationMs).toLong())
                    }
                }
            }
            .pointerInput(durationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekTo((dragFraction * durationMs).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 3.5.dp.toPx()
            val availableBarWidth = (totalWidth - (barCount - 1) * spacing) / barCount
            val barWidth = availableBarWidth.coerceAtLeast(2.5.dp.toPx())
            val maxBarHeight = totalHeight * 0.85f
            val minBarHeight = 6.dp.toPx()

            for (i in 0 until barCount) {
                val fractionAtBar = i.toFloat() / barCount.toFloat()
                val isActive = fractionAtBar <= displayFraction

                val baseAmp = baseAmplitudes[i]
                val dynamicBounce = if (isPlaying && isActive) {
                    val dynamicOffset = sin(waveBouncePhase + i * 0.35f) * 0.15f
                    (baseAmp + dynamicOffset.toFloat()).coerceIn(0.2f, 1.0f)
                } else {
                    baseAmp
                }

                val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * dynamicBounce
                val x = i * (barWidth + spacing)
                val y = (totalHeight - barHeight) / 2f

                val color = if (isActive) {
                    activeColor
                } else {
                    inactiveColor
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
