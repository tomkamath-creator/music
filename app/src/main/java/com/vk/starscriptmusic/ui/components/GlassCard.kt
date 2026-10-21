package com.vk.starscriptmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.GlassBorder

/**
 * Neo-Groove Frosted Glass Card with luminous subtle gradient border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = CardSurface.copy(alpha = 0.75f),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBorder = borderBrush ?: Brush.linearGradient(
        colors = listOf(borderColor, borderColor.copy(alpha = 0.1f))
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, effectiveBorder, shape),
        content = content
    )
}
