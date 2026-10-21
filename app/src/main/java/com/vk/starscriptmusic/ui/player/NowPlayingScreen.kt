package com.vk.starscriptmusic.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vk.starscriptmusic.R
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.core.theme.TextTertiary
import com.vk.starscriptmusic.ui.components.FlowingHaloBox
import com.vk.starscriptmusic.ui.components.SoundwaveProgressBar
import com.vk.starscriptmusic.ui.components.bouncyClick
import java.util.Locale
import kotlin.math.abs

/**
 * Full-screen Now Playing screen with Neo-Groove Dark Pop aesthetic:
 * - Static smooth rounded-rectangle album art (24dp corner radius) with dynamic Palette back-glow
 * - Horizontal swipe-to-skip gestures across album art
 * - Signature flowing neon halo encircling Song Title and Artist Name
 * - 10-second skip buttons (-10s / +10s) flanking the main Play/Pause button
 * - Soundwave progression bar with floating timestamp pill
 * - Expandable frosted-glass Song Intel & Trivia Card with public online intelligence and offline fallback
 */
@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackState = uiState.playbackState
    val track = playbackState.currentTrack ?: return
    val palette = uiState.palette
    val context = LocalContext.current

    var isIntelExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "album_art_breathing")
    val artGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "art_glow"
    )

    // Swipe-to-skip gesture state
    var totalDragX by remember { mutableFloatStateOf(0f) }

    val artModel = track.albumArtUri ?: uiState.songIntel?.highResArtworkUrl ?: R.drawable.ic_default_art

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.darkBackdrop,
                        OledBlack,
                        OledBlack
                    )
                )
            )
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .bouncyClick(onClick = onDismiss)
                        .clip(CircleShape)
                        .background(CardSurface.copy(alpha = 0.8f))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = palette.secondaryAccent
                    )
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite() },
                    modifier = Modifier
                        .size(42.dp)
                        .bouncyClick(onClick = { viewModel.toggleFavorite() })
                        .clip(CircleShape)
                        .background(CardSurface.copy(alpha = 0.8f))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isFavorite) NeoMagenta else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Static, Smooth Rounded-Rectangle Album Art Card (24dp corner radius)
            // with Ambient Back-Glow & Horizontal Swipe-to-Skip Gesture
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDragX += dragAmount
                            },
                            onDragEnd = {
                                if (totalDragX < -80f) {
                                    // Swiped Left -> Skip Next
                                    viewModel.skipNext()
                                } else if (totalDragX > 80f) {
                                    // Swiped Right -> Skip Previous / Replay
                                    viewModel.skipPrevious()
                                }
                                totalDragX = 0f
                            },
                            onDragCancel = { totalDragX = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Dynamic Ambient Colored Back-Glow sampled from Palette API
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f * artGlowPulse)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    palette.primaryAccent.copy(alpha = 0.55f),
                                    palette.secondaryAccent.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(52.dp)
                )

                // Foreground Static 24dp Rounded-Rectangle Cover Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceElevated)
                        .border(
                            width = 1.8.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    palette.primaryAccent.copy(alpha = 0.7f),
                                    Color(0x33FFFFFF)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artModel)
                            .crossfade(true)
                            .error(R.drawable.ic_default_art)
                            .fallback(R.drawable.ic_default_art)
                            .build(),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Signature Flowing Halo Encircling Track Title & Artist
            FlowingHaloBox(
                modifier = Modifier.fillMaxWidth(),
                primaryColor = palette.primaryAccent,
                secondaryColor = palette.secondaryAccent,
                tertiaryColor = NeoMagenta
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = palette.secondaryAccent,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Minimal, Responsive Soundwave Progression Bar & Timestamp Displays
            Column(modifier = Modifier.fillMaxWidth()) {
                SoundwaveProgressBar(
                    currentPositionMs = playbackState.currentPositionMs,
                    durationMs = playbackState.durationMs,
                    isPlaying = playbackState.isPlaying,
                    onSeekTo = { viewModel.seekTo(it) },
                    activeColor = palette.primaryAccent,
                    barCount = 38
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playbackState.currentPositionMs),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )

                    val remainingMs = (playbackState.durationMs - playbackState.currentPositionMs).coerceAtLeast(0L)
                    Text(
                        text = if (playbackState.durationMs > 0) "-${formatTime(remainingMs)}" else "LIVE",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls: [Shuffle] [Prev] [-10s] [Play/Pause] [+10s] [Next] [Repeat]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffleModeEnabled) palette.primaryAccent else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous Track Button
                IconButton(
                    onClick = { viewModel.skipPrevious() },
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClick(onClick = { viewModel.skipPrevious() })
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Dedicated Rewind 10s Button (-10s)
                IconButton(
                    onClick = { viewModel.seekRelative(-10_000L) },
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClick(onClick = { viewModel.seekRelative(-10_000L) })
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = palette.secondaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Main Play / Pause Large Glowing Bouncy Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClick(onClick = { viewModel.playPause() })
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(palette.primaryAccent, palette.secondaryAccent)
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Dedicated Fast-Forward 10s Button (+10s)
                IconButton(
                    onClick = { viewModel.seekRelative(10_000L) },
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClick(onClick = { viewModel.seekRelative(10_000L) })
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Fast-Forward 10s",
                        tint = palette.secondaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Next Track Button
                IconButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClick(onClick = { viewModel.skipNext() })
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(38.dp)
                ) {
                    val repeatIcon = if (playbackState.repeatMode == Player.REPEAT_MODE_ONE) {
                        Icons.Default.RepeatOne
                    } else {
                        Icons.Default.Repeat
                    }
                    val isRepeatActive = playbackState.repeatMode != Player.REPEAT_MODE_OFF

                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) palette.primaryAccent else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Expandable Frosted-Glass "Song Intel & Trivia" Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClick(onClick = { isIntelExpanded = !isIntelExpanded })
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurface.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                palette.primaryAccent.copy(alpha = 0.5f),
                                Color(0x22FFFFFF)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = palette.secondaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SONG INTEL & TRIVIA",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = TextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.isIntelLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = palette.secondaryAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = if (isIntelExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Song Intel",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isIntelExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            val intel = uiState.songIntel

                            // Meta Tags: Release Year, Genre, Studio Format
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IntelPill(
                                    label = "YEAR",
                                    value = intel?.releaseYear ?: "N/A",
                                    accentColor = palette.primaryAccent
                                )
                                IntelPill(
                                    label = "GENRE",
                                    value = intel?.genre ?: "Audio",
                                    accentColor = palette.secondaryAccent
                                )
                                IntelPill(
                                    label = "QUALITY",
                                    value = if (intel?.highResArtworkUrl != null) "HD 1400p" else "Lossless",
                                    accentColor = NeoMagenta
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Label / Publisher
                            if (!intel?.recordLabel.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Label: ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = intel?.recordLabel ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Trivia snippet
                            Text(
                                text = intel?.trivia
                                    ?: "Offline Intel: Playing directly from local storage. Embedded ID3 metadata active.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun IntelPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurfaceElevated)
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                ),
                color = accentColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
