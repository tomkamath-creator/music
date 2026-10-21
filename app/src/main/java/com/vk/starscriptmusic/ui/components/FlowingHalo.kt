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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet

/**
 * Signature Neo-Groove Flowing Neon Halo.
 * Encircles text or cards with an animated, fluid, rotating luminous aura.
 */
@Composable
fun FlowingHaloBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    primaryColor: Color = NeoViolet,
    secondaryColor: Color = NeoCyan,
    tertiaryColor: Color = NeoMagenta,
    haloBlur: Dp = 20.dp,
    borderWidth: Dp = 1.8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_flow")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    val sweepGradient = Brush.sweepGradient(
        colors = listOf(
            primaryColor,
            secondaryColor,
            tertiaryColor,
            primaryColor
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Multi-layered fluid ambient back-glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .rotate(rotationAngle)
                .clip(RoundedCornerShape(cornerRadius))
                .background(sweepGradient)
                .blur(haloBlur)
        )

        // Organic crisp outer neon halo stroke
        Box(
            modifier = Modifier
                .matchParentSize()
                .rotate(rotationAngle)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    width = borderWidth,
                    brush = sweepGradient,
                    shape = RoundedCornerShape(cornerRadius)
                )
        )

        // Frosted Card interior holding the track content
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(CardSurface.copy(alpha = 0.88f))
                .padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}
