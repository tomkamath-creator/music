package com.vk.starscriptmusic.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.HolographicBronze
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.HolographicSilver
import com.vk.starscriptmusic.core.theme.TextSecondary
import java.util.Locale

/**
 * Editorial Holographic Rank Ring for the Top 50 Leaderboard ("Hall of Fame").
 * Renders shimmering Liquid Gold for #1, Starlight Silver for #2,
 * Burnished Bronze for #3, and clean typographic indicators for #4–#50.
 */
@Composable
fun HolographicRankRing(
    rank: Int,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_ring")
    val shimmerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    when (rank) {
        1 -> {
            // #1 Track: Liquid Gold Shimmering Ring
            val goldBrush = Brush.sweepGradient(
                colors = listOf(
                    HolographicGold,
                    Color(0xFFFFF4B8),
                    Color(0xFFB8860B),
                    Color(0xFFFFDF7A),
                    HolographicGold
                )
            )

            Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
                // Outer Golden Glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(HolographicGold.copy(alpha = 0.35f))
                        .blur(14.dp)
                )

                // Shimmering Ring Border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .rotate(shimmerRotation)
                        .clip(CircleShape)
                        .border(2.5.dp, goldBrush, CircleShape)
                )

                // Inner Disc
                Box(
                    modifier = Modifier
                        .size(size - 6.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "01",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = HolographicGold
                        )
                    )
                }
            }
        }
        2 -> {
            // #2 Track: Starlight Silver Ring
            val silverBrush = Brush.sweepGradient(
                colors = listOf(
                    HolographicSilver,
                    Color.White,
                    Color(0xFF94A3B8),
                    Color(0xFFF1F5F9),
                    HolographicSilver
                )
            )

            Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
                // Outer Silver Glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(HolographicSilver.copy(alpha = 0.25f))
                        .blur(12.dp)
                )

                // Shimmering Ring Border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .rotate(shimmerRotation)
                        .clip(CircleShape)
                        .border(2.dp, silverBrush, CircleShape)
                )

                // Inner Disc
                Box(
                    modifier = Modifier
                        .size(size - 6.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "02",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = HolographicSilver
                        )
                    )
                }
            }
        }
        3 -> {
            // #3 Track: Burnished Bronze / Rose-Copper Ring
            val bronzeBrush = Brush.sweepGradient(
                colors = listOf(
                    HolographicBronze,
                    Color(0xFFE89C5D),
                    Color(0xFF8B4513),
                    Color(0xFFD27D2D),
                    HolographicBronze
                )
            )

            Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
                // Outer Bronze Glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(HolographicBronze.copy(alpha = 0.22f))
                        .blur(10.dp)
                )

                // Shimmering Ring Border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .rotate(shimmerRotation)
                        .clip(CircleShape)
                        .border(2.dp, bronzeBrush, CircleShape)
                )

                // Inner Disc
                Box(
                    modifier = Modifier
                        .size(size - 6.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "03",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = HolographicBronze
                        )
                    )
                }
            }
        }
        else -> {
            // #4 to #50: Clean typographic indicator
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(CardSurface.copy(alpha = 0.6f))
                    .border(1.dp, Color(0x22FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.US, "%02d", rank),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                )
            }
        }
    }
}
