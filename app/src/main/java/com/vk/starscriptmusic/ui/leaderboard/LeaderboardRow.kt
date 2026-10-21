package com.vk.starscriptmusic.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vk.starscriptmusic.R
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.core.theme.TextTertiary
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.ui.components.HolographicRankRing
import com.vk.starscriptmusic.ui.components.bouncyClick

/**
 * Editorial row for the Top 50 Leaderboard ("Hall of Fame").
 * Incorporates Holographic Rank Rings and clean capsule play counters with zero emojis.
 */
@Composable
fun LeaderboardRow(
    track: Track,
    rank: Int,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onTrackClick)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isPlaying) CardSurfaceElevated else CardSurface.copy(alpha = 0.65f))
            .border(
                width = if (rank == 1) 1.5.dp else 1.dp,
                color = when {
                    isPlaying -> NeoViolet
                    rank == 1 -> HolographicGold.copy(alpha = 0.5f)
                    else -> Color(0x1AFFFFFF)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Holographic Rank Indicator
        HolographicRankRing(rank = rank)

        Spacer(modifier = Modifier.width(14.dp))

        // Album Art Thumbnail
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(track.albumArtUri ?: R.drawable.ic_default_art)
                    .crossfade(true)
                    .error(R.drawable.ic_default_art)
                    .fallback(R.drawable.ic_default_art)
                    .build(),
                contentDescription = track.album,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Playing",
                        tint = NeoViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Track title & artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isPlaying) NeoViolet else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Clean Editorial Play Counter Capsule Tag (No emojis)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurfaceElevated)
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${track.playCount} ${if (track.playCount == 1) "PLAY" else "PLAYS"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.sp
                ),
                color = when (rank) {
                    1 -> HolographicGold
                    2 -> Color(0xFFE2E8F0)
                    3 -> Color(0xFFCD7F32)
                    else -> TextSecondary
                }
            )
        }

        // Favorite Toggle
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) NeoMagenta else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
