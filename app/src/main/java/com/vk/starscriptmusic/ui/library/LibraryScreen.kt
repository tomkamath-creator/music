package com.vk.starscriptmusic.ui.library

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.TextMuted
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.ui.components.ArtistHubCard
import com.vk.starscriptmusic.ui.components.GlassCard
import com.vk.starscriptmusic.ui.components.TrackItemRow
import com.vk.starscriptmusic.ui.components.bouncyClick

import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import com.vk.starscriptmusic.core.theme.HolographicGold

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "halo_ambient")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
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
        // Subtle ambient halo back-glow at the top
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeoViolet.copy(alpha = pulseAlpha),
                            NeoCyan.copy(alpha = pulseAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header Bar with Signature Starscript Aura
            HeaderSection(
                isScanning = uiState.isScanning,
                onSyncClick = { viewModel.syncLibrary() },
                onLeaderboardClick = onLeaderboardClick,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBarSection(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs (Songs, Artists, Playlists, Favorites)
            TabRowSection(
                selectedTab = uiState.selectedTab,
                songCount = uiState.tracks.size,
                artistCount = uiState.artists.size,
                playlistCount = uiState.playlists.size,
                favoriteCount = uiState.favorites.size,
                onTabSelect = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (uiState.selectedTab) {
                LibraryTab.SONGS -> {
                    SongsTabContent(
                        tracks = uiState.tracks,
                        currentPlayingTrackId = uiState.playbackState.currentTrack?.id,
                        onTrackClick = { track -> viewModel.playTrack(track, uiState.tracks) },
                        onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                        onPlayAll = { viewModel.playAll(uiState.tracks) },
                        onShuffleAll = { viewModel.shuffleAll(uiState.tracks) }
                    )
                }
                LibraryTab.ARTISTS -> {
                    ArtistsTabContent(
                        artists = uiState.artists,
                        onArtistClick = onArtistClick
                    )
                }
                LibraryTab.PLAYLISTS -> {
                    PlaylistsTabContent(
                        playlists = uiState.playlists,
                        onPlaylistClick = onPlaylistClick,
                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
                    )
                }
                LibraryTab.FAVORITES -> {
                    SongsTabContent(
                        tracks = uiState.favorites,
                        currentPlayingTrackId = uiState.playbackState.currentTrack?.id,
                        onTrackClick = { track -> viewModel.playTrack(track, uiState.favorites) },
                        onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                        onPlayAll = { viewModel.playAll(uiState.favorites) },
                        onShuffleAll = { viewModel.shuffleAll(uiState.favorites) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isScanning: Boolean,
    onSyncClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "STARSCRIPT MUSIC",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "by vk",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    color = NeoCyan,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Hall of Fame Leaderboard Button
            IconButton(
                onClick = onLeaderboardClick,
                modifier = Modifier
                    .size(42.dp)
                    .bouncyClick(onClick = onLeaderboardClick)
                    .clip(CircleShape)
                    .background(CardSurface.copy(alpha = 0.8f))
                    .border(1.2.dp, HolographicGold.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = "Hall of Fame Top 50",
                    tint = HolographicGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sync Library Button
            IconButton(
                onClick = onSyncClick,
                modifier = Modifier
                    .size(42.dp)
                    .bouncyClick(onClick = onSyncClick)
                    .clip(CircleShape)
                    .background(CardSurface.copy(alpha = 0.8f))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Library",
                    tint = if (isScanning) NeoCyan else TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(42.dp)
                    .bouncyClick(onClick = onSettingsClick)
                    .clip(CircleShape)
                    .background(CardSurface.copy(alpha = 0.8f))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search tracks, artists, albums...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardSurfaceElevated.copy(alpha = 0.7f),
            unfocusedContainerColor = CardSurface.copy(alpha = 0.5f),
            focusedBorderColor = NeoViolet,
            unfocusedBorderColor = Color(0x22FFFFFF),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeoViolet
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Composable
private fun TabRowSection(
    selectedTab: LibraryTab,
    songCount: Int,
    artistCount: Int,
    playlistCount: Int,
    favoriteCount: Int,
    onTabSelect: (LibraryTab) -> Unit
) {
    val tabs = listOf(
        LibraryTab.SONGS to "Songs ($songCount)",
        LibraryTab.ARTISTS to "Artists ($artistCount)",
        LibraryTab.PLAYLISTS to "Playlists ($playlistCount)",
        LibraryTab.FAVORITES to "Favorites ($favoriteCount)"
    )

    val selectedIndex = LibraryTab.entries.indexOf(selectedTab)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = NeoViolet,
        edgePadding = 20.dp,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 2.5.dp,
                    color = NeoViolet
                )
            }
        },
        divider = {}
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            )
        }
    }
}

@Composable
private fun SongsTabContent(
    tracks: List<Track>,
    currentPlayingTrackId: Long?,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Audiotrack,
            message = "No songs found in your local audio library"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            QuickActionsBar(
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll
            )
        }

        items(tracks, key = { it.id }) { track ->
            TrackItemRow(
                track = track,
                isPlaying = track.id == currentPlayingTrackId,
                onTrackClick = { onTrackClick(track) },
                onFavoriteClick = { onFavoriteClick(track) }
            )
        }
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<com.vk.starscriptmusic.domain.model.Artist>,
    onArtistClick: (String) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Audiotrack,
            message = "No artists found. Tap Sync to discover artist hubs."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "AUTO-GENERATED ARTIST HUBS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = NeoCyan,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        items(artists, key = { it.name }) { artist ->
            ArtistHubCard(
                artist = artist,
                onArtistClick = { onArtistClick(artist.name) }
            )
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeoViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeoViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create New Playlist",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClick { onPlaylistClick(playlist.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = if (playlist.isAutoGenerated) NeoCyan else NeoViolet,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = "${playlist.trackCount} tracks • ${if (playlist.isAutoGenerated) "Auto Hub" else "Custom"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlayAll,
            colors = ButtonDefaults.buttonColors(containerColor = NeoViolet),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Play All",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
            )
        }

        Button(
            onClick = onShuffleAll,
            colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
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

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
