package com.vk.starscriptmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.domain.model.Artist

/**
 * Visual card representing an auto-generated artist hub with color-coded halo outline.
 */
@Composable
fun ArtistHubCard(
    artist: Artist,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = artist.accentColorArgb?.let { Color(it) } ?: NeoViolet

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onArtistClick)
            .clip(RoundedCornerShape(22.dp))
            .background(CardSurface.copy(alpha = 0.85f))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.9f),
                        accentColor.copy(alpha = 0.2f),
                        Color(0x1AFFFFFF)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {
        // Ambient glow behind thumbnail
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.25f))
                .blur(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Artist Art with Color-coded halo border
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(2.dp, accentColor, CircleShape)
                    .background(CardSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (artist.representativeArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artist.representativeArtUri)
                            .crossfade(true)
                            .error(R.drawable.ic_default_art)
                            .fallback(R.drawable.ic_default_art)
                            .build(),
                        contentDescription = artist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Artist Name and Metadata Pills
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Track Count Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${artist.trackCount} ${if (artist.trackCount == 1) "TRACK" else "TRACKS"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = accentColor
                        )
                    }

                    // Album Count Tag
                    Text(
                        text = "${artist.albumCount} ${if (artist.albumCount == 1) "Album" else "Albums"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Hub",
                tint = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
