package com.vk.starscriptmusic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.TextPrimary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Minimal, responsive Soundwave Progression Bar.
 * Features an animated multi-bar wave silhouette that subtly ripples during playback,
 * smooth scrub/drag interaction, and a floating timestamp pill positioned above the touch point.
 */
@Composable
fun SoundwaveProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit,
    activeColor: Color = NeoViolet,
    inactiveColor: Color = Color(0x33FFFFFF),
    barCount: Int = 38,
    modifier: Modifier = Modifier
) {
    var componentWidthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var dragPositionPx by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayProgress = if (isDragging) dragProgress else actualProgress

    val infiniteTransition = rememberInfiniteTransition(label = "soundwave_ripple")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Base waveform envelope heights (normalized between 0.22f and 1.0f)
    val baseWaveWeights = remember(barCount) {
        FloatArray(barCount) { i ->
            val norm = i.toFloat() / (barCount - 1)
            // Combine harmonics for a pleasing organic studio audio wave silhouette
            val h1 = sin(norm * PI).toFloat()
            val h2 = sin(norm * 3.5 * PI).toFloat() * 0.25f
            val h3 = sin(norm * 7.0 * PI).toFloat() * 0.15f
            (h1 + h2 + h3).coerceIn(0.20f, 1.0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .onSizeChanged { componentWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / componentWidthPx).coerceIn(0f, 1f)
                    if (durationMs > 0) {
                        onSeekTo((fraction * durationMs).toLong())
                    }
                }
            }
            .pointerInput(durationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragPositionPx = offset.x
                        dragProgress = (offset.x / componentWidthPx).coerceIn(0f, 1f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragPositionPx = change.position.x
                        dragProgress = (change.position.x / componentWidthPx).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        if (durationMs > 0) {
                            onSeekTo((dragProgress * durationMs).toLong())
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
    ) {
        // Floating Timestamp Pill above touch/drag point
        if (isDragging && durationMs > 0) {
            val previewMs = (dragProgress * durationMs).toLong().coerceIn(0L, durationMs)
            val pillWidthPx = 180f
            val pillOffsetXPx = (dragPositionPx - (pillWidthPx / 2f)).coerceIn(
                0f,
                (componentWidthPx - pillWidthPx).coerceAtLeast(0f)
            )

            Box(
                modifier = Modifier
                    .offset { IntOffset(pillOffsetXPx.roundToInt(), -30) }
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, activeColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = formatSoundwaveTime(previewMs),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = TextPrimary
                )
            }
        }

        // Soundwave Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .align(Alignment.BottomCenter)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 3.5.dp.toPx()
            val availableWidth = totalWidth - (spacing * (barCount - 1))
            val barWidth = (availableWidth / barCount).coerceAtLeast(2.dp.toPx())

            for (i in 0 until barCount) {
                val barFraction = i.toFloat() / barCount
                val isActive = barFraction <= displayProgress

                // Ripple height modulation during playback
                val rippleMod = if (isPlaying) {
                    val angle = wavePhase + (i * 0.35f)
                    (sin(angle) * 0.18f).toFloat()
                } else 0f

                val heightFraction = (baseWaveWeights[i] + rippleMod).coerceIn(0.18f, 1.0f)
                val barHeight = totalHeight * heightFraction
                val x = i * (barWidth + spacing)
                val y = (totalHeight - barHeight) / 2f

                val barColor = if (isActive) {
                    activeColor
                } else {
                    inactiveColor
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}

private fun formatSoundwaveTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
