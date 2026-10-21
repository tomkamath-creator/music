package com.vk.starscriptmusic.ui.radio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.TextMuted
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.core.theme.TextTertiary
import com.vk.starscriptmusic.data.network.RadioStation
import com.vk.starscriptmusic.ui.components.bouncyClick

/**
 * Built-in Global Internet Radio Station Browser and Player.
 * Streams stations directly via Jetpack Media3 ExoPlayer.
 */
@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurface.copy(alpha = 0.8f)),
                placeholder = {
                    Text(
                        text = "Search radio stations or genres...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeoViolet
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeoViolet,
                    unfocusedBorderColor = Color(0x22FFFFFF),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeoCyan
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Genre Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.genres.forEach { genre ->
                val isSelected = uiState.selectedGenre.equals(genre, ignoreCase = true) && uiState.searchQuery.isBlank()

                Box(
                    modifier = Modifier
                        .bouncyClick(onClick = { viewModel.selectGenre(genre) })
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(NeoViolet, NeoCyan))
                            } else {
                                Brush.horizontalGradient(listOf(CardSurface, CardSurface))
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.3f) else Color(0x22FFFFFF),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Station List Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NeoCyan,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (uiState.stations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No radio stations found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try another genre tag or check your network connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.stations,
                    key = { it.stationuuid }
                ) { station ->
                    val isPlayingThisStation = uiState.playbackState.currentTrack?.contentUri == station.urlResolved &&
                            uiState.playbackState.isPlaying

                    RadioStationRow(
                        station = station,
                        isPlaying = isPlayingThisStation,
                        onPlayClick = { viewModel.playStation(station) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
private fun RadioStationRow(
    station: RadioStation,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onPlayClick)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isPlaying) CardSurfaceElevated else CardSurface.copy(alpha = 0.7f))
            .border(
                1.dp,
                if (isPlaying) NeoCyan else Color(0x1AFFFFFF),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Station Logo / Favicon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(station.favicon?.ifBlank { null } ?: R.drawable.ic_default_art)
                    .crossfade(true)
                    .error(R.drawable.ic_default_art)
                    .fallback(R.drawable.ic_default_art)
                    .build(),
                contentDescription = station.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Playing",
                        tint = NeoCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Station Details: Name, Country, Tags
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isPlaying) NeoCyan else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val metaText = listOfNotNull(
                station.country?.takeIf { it.isNotBlank() },
                station.codec?.takeIf { it.isNotBlank() }?.uppercase(),
                if (station.bitrate > 0) "${station.bitrate} kbps" else null
            ).joinToString(" • ")

            Text(
                text = metaText.ifBlank { "Live Internet Broadcast" },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play Action Indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isPlaying) NeoCyan else CardSurfaceElevated)
                .border(1.dp, Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                contentDescription = "Play Station",
                tint = if (isPlaying) Color.Black else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
