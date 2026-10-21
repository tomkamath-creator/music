package com.vk.starscriptmusic.ui.leaderboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.DeepSpaceCharcoal
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.TextMuted
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.ui.components.bouncyClick

/**
 * Top 50 Leaderboard ("Hall of Fame").
 * Renders verified >= 50% played tracks with Liquid Gold, Starlight Silver,
 * and Burnished Bronze holographic rings.
 */
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val topTracks = uiState.topTracks
    val currentTrackId = uiState.playbackState.currentTrack?.id

    val infiniteTransition = rememberInfiniteTransition(label = "gold_ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_gold"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OledBlack, DeepSpaceCharcoal, OledBlack)
                )
            )
    ) {
        // Liquid Gold ambient aura at top
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HolographicGold.copy(alpha = ambientPulse * 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .blur(72.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .bouncyClick(onClick = onBackClick)
                            .clip(CircleShape)
                            .background(CardSurface.copy(alpha = 0.8f))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "HALL OF FAME",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = "TOP 50 MOST PLAYED TRACKS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.6.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = HolographicGold
                        )
                    }
                }
            }

            if (topTracks.isNotEmpty()) {
                item {
                    // Quick Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.playAll() },
                            colors = ButtonDefaults.buttonColors(containerColor = HolographicGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Play Top 50",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.shuffleAll() },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = NeoCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Shuffle",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                itemsIndexed(topTracks, key = { _, track -> track.id }) { index, track ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        LeaderboardRow(
                            track = track,
                            rank = index + 1,
                            isPlaying = track.id == currentTrackId,
                            onTrackClick = { viewModel.playTrack(track) },
                            onFavoriteClick = { viewModel.toggleFavorite(track) }
                        )
                    }
                }
            } else {
                item {
                    // Editorial Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(CardSurfaceElevated)
                                    .border(1.5.dp, HolographicGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = HolographicGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "THE HALL OF FAME IS EMPTY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimary
                            )

                            Text(
                                text = "Listen to at least 50% of any track to automatically qualify for the Top 50 Leaderboard rankings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
