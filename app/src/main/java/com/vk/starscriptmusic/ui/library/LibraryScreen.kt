package com.vk.starscriptmusic.ui.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.DeepSpaceCharcoal
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.TextMuted
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.core.theme.TextTertiary
import com.vk.starscriptmusic.domain.model.Artist
import com.vk.starscriptmusic.domain.model.Playlist
import com.vk.starscriptmusic.domain.model.Track
import com.vk.starscriptmusic.ui.components.ArtistHubCard
import com.vk.starscriptmusic.ui.components.GlassCard
import com.vk.starscriptmusic.ui.components.TrackItemRow
import com.vk.starscriptmusic.ui.components.bouncyClick
import com.vk.starscriptmusic.ui.leaderboard.LeaderboardRow
import com.vk.starscriptmusic.ui.radio.RadioScreen
import com.vk.starscriptmusic.ui.radio.RadioViewModel

/**
 * Main Library Screen inspired by the clean, responsive aesthetic of Oto Music Plus.
 * Houses 5 Labeled Tabs with smooth spring transitions:
 * 1. Songs (with fast scroll, sticky headers, multi-attribute sort sheet)
 * 2. Playlists (manual playlist manager)
 * 3. Artists (clean discography directory)
 * 4. Most Played (Hall of Fame leaderboard with holographic rings & exact count pills)
 * 5. Radio (built-in global internet radio browser and player)
 */
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
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Starscript Ambient Aura
        Box(
            modifier = Modifier
                .size(340.dp)
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
            // Header Bar
            HeaderSection(
                isScanning = uiState.isScanning,
                onSyncClick = { viewModel.syncLibrary() },
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar (shown for Songs, Playlists, and Artists)
            if (uiState.selectedTab != LibraryTab.RADIO) {
                SearchBarSection(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5 Labeled Navigation Tabs with Spring Transitions
            TabRowSection(
                selectedTab = uiState.selectedTab,
                songCount = uiState.tracks.size,
                playlistCount = uiState.playlists.size,
                artistCount = uiState.artists.size,
                mostPlayedCount = uiState.mostPlayedTracks.size,
                onTabSelect = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content with Animated Transition
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "tab_content_transition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    LibraryTab.SONGS -> {
                        SongsTabContent(
                            tracks = uiState.tracks,
                            currentSort = uiState.sortOption,
                            currentPlayingTrackId = uiState.playbackState.currentTrack?.id,
                            onTrackClick = { track -> viewModel.playTrack(track, uiState.tracks) },
                            onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                            onPlayAll = { viewModel.playAll(uiState.tracks) },
                            onShuffleAll = { viewModel.shuffleAll(uiState.tracks) },
                            onSortSelect = { viewModel.setSortOption(it) }
                        )
                    }
                    LibraryTab.PLAYLISTS -> {
                        PlaylistsTabContent(
                            playlists = uiState.playlists,
                            onPlaylistClick = onPlaylistClick,
                            onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                            onRenamePlaylist = { id, newName -> viewModel.renamePlaylist(id, newName) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                        )
                    }
                    LibraryTab.ARTISTS -> {
                        ArtistsTabContent(
                            artists = uiState.artists,
                            onArtistClick = onArtistClick
                        )
                    }
                    LibraryTab.MOST_PLAYED -> {
                        MostPlayedTabContent(
                            tracks = uiState.mostPlayedTracks,
                            currentPlayingTrackId = uiState.playbackState.currentTrack?.id,
                            onTrackClick = { track -> viewModel.playTrack(track, uiState.mostPlayedTracks) },
                            onFavoriteClick = { track -> viewModel.toggleFavorite(track) }
                        )
                    }
                    LibraryTab.RADIO -> {
                        val radioViewModel: RadioViewModel = hiltViewModel()
                        RadioScreen(viewModel = radioViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isScanning: Boolean,
    onSyncClick: () -> Unit,
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
    playlistCount: Int,
    artistCount: Int,
    mostPlayedCount: Int,
    onTabSelect: (LibraryTab) -> Unit
) {
    val tabs = listOf(
        Triple(LibraryTab.SONGS, "Songs", Icons.Default.Audiotrack),
        Triple(LibraryTab.PLAYLISTS, "Playlists", Icons.Default.QueueMusic),
        Triple(LibraryTab.ARTISTS, "Artists", Icons.Default.Mic),
        Triple(LibraryTab.MOST_PLAYED, "Most Played", Icons.Default.Leaderboard),
        Triple(LibraryTab.RADIO, "Radio", Icons.Default.Radio)
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
        tabs.forEach { (tab, title, icon) ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) NeoViolet else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTabContent(
    tracks: List<Track>,
    currentSort: SortOption,
    currentPlayingTrackId: Long?,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSortSelect: (SortOption) -> Unit
) {
    var showSortDialog by remember { mutableStateOf(false) }

    if (tracks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Audiotrack,
            message = "No songs found in your local audio library"
        )
        return
    }

    // Group tracks for sticky headers (by first letter for title/artist, or single group)
    val groupedTracks = remember(tracks, currentSort) {
        when (currentSort) {
            SortOption.TITLE_AZ, SortOption.TITLE_ZA -> {
                tracks.groupBy { it.title.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            }
            SortOption.ARTIST -> {
                tracks.groupBy { it.artist.ifBlank { "Unknown Artist" } }
            }
            else -> mapOf("ALL TRACKS" to tracks)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action Bar & Multi-Attribute Sort Selector
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(containerColor = NeoViolet),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Play All", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onShuffleAll,
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(42.dp).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = NeoCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Shuffle", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                    }

                    // Sort Pill
                    Box(
                        modifier = Modifier
                            .bouncyClick(onClick = { showSortDialog = true })
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardSurface)
                            .border(1.dp, NeoViolet.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = NeoViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        groupedTracks.forEach { (header, headerTracks) ->
            if (groupedTracks.size > 1) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OledBlack.copy(alpha = 0.95f))
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = NeoCyan
                        )
                    }
                }
            }

            items(headerTracks, key = { it.id }) { track ->
                TrackItemRow(
                    track = track,
                    isPlaying = track.id == currentPlayingTrackId,
                    onTrackClick = { onTrackClick(track) },
                    onFavoriteClick = { onFavoriteClick(track) }
                )
            }
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            containerColor = CardSurfaceElevated,
            title = {
                Text(
                    text = "Sort Songs By",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    SortOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSortSelect(option)
                                    showSortDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSort == option,
                                onClick = {
                                    onSortSelect(option)
                                    showSortDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeoViolet,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (currentSort == option) NeoViolet else TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close", color = NeoCyan)
                }
            }
        )
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var createNameText by remember { mutableStateOf("") }

    var renamePlaylistTarget by remember { mutableStateOf<Playlist?>(null) }
    var renameText by remember { mutableStateOf("") }

    var deletePlaylistTarget by remember { mutableStateOf<Playlist?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = {
                    createNameText = ""
                    showCreateDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
                    text = "Create Manual Playlist",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (playlists.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.QueueMusic,
                    message = "No playlists created yet. Tap 'Create Manual Playlist' to build one."
                )
            }
        } else {
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = NeoViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${playlist.trackCount} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // Rename button
                        IconButton(
                            onClick = {
                                renamePlaylistTarget = playlist
                                renameText = playlist.name
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { deletePlaylistTarget = playlist },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = NeoMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CardSurfaceElevated,
            title = {
                Text("Create Playlist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            },
            text = {
                OutlinedTextField(
                    value = createNameText,
                    onValueChange = { createNameText = it },
                    placeholder = { Text("Playlist Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoViolet,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (createNameText.isNotBlank()) {
                            onCreatePlaylist(createNameText.trim())
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create", color = NeoCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Rename Dialog
    renamePlaylistTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renamePlaylistTarget = null },
            containerColor = CardSurfaceElevated,
            title = {
                Text("Rename Playlist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoViolet,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenamePlaylist(target.id, renameText.trim())
                            renamePlaylistTarget = null
                        }
                    }
                ) {
                    Text("Save", color = NeoCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamePlaylistTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Delete Dialog
    deletePlaylistTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deletePlaylistTarget = null },
            containerColor = CardSurfaceElevated,
            title = {
                Text("Delete Playlist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${target.name}'?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(target.id)
                        deletePlaylistTarget = null
                    }
                ) {
                    Text("Delete", color = NeoMagenta, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletePlaylistTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<Artist>,
    onArtistClick: (String) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Audiotrack,
            message = "No artists found in local library. Tap Sync to discover."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artists, key = { it.name }) { artist ->
            ArtistHubCard(
                artist = artist,
                onArtistClick = { onArtistClick(artist.name) }
            )
        }
    }
}

@Composable
private fun MostPlayedTabContent(
    tracks: List<Track>,
    currentPlayingTrackId: Long?,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Leaderboard,
            message = "No tracks played yet. Listen to at least 50% of any song to see it ranked here!"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "MOST PLAYED LEADERBOARD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = HolographicGold
                )
            }
        }

        items(tracks.size) { index ->
            val track = tracks[index]
            val rank = index + 1
            LeaderboardRow(
                track = track,
                rank = rank,
                isPlaying = track.id == currentPlayingTrackId,
                onTrackClick = { onTrackClick(track) },
                onFavoriteClick = { onFavoriteClick(track) }
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
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
