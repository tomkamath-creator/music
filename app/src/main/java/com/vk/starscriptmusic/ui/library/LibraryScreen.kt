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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vk.starscriptmusic.R
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.DarkSurface
import com.vk.starscriptmusic.core.theme.DeepSpaceCharcoal
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoElectricBlue
import com.vk.starscriptmusic.core.theme.NeoLime
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
import com.vk.starscriptmusic.ui.radio.RadioScreen
import com.vk.starscriptmusic.ui.radio.RadioViewModel
import kotlinx.coroutines.launch

/**
 * Main Library & Music Hub with Bottom Navigation Bar & Navigation Drawer directly inspired by Oto Music Plus:
 * - Bottom Navigation Bar (Home, Songs, Playlists, Artists, Radio)
 * - Navigation Drawer with Now Playing header, Listening stats, Library count, Sync, Settings
 * - Home Tab with Quick Action Circles (History, Favorites, Most Played, Shuffle) & Smart Mix Cards
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onLeaderboardClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAboutDialog by remember { mutableStateOf(false) }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepSpaceCharcoal,
                modifier = Modifier.width(310.dp)
            ) {
                // Drawer Header with Current Track Info (Image 1)
                val currentTrack = uiState.playbackState.currentTrack
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardSurfaceElevated)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentTrack?.albumArtUri ?: R.drawable.ic_default_art)
                                    .crossfade(true)
                                    .error(R.drawable.ic_default_art)
                                    .fallback(R.drawable.ic_default_art)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack?.title ?: "Starscript Music",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack?.artist ?: "by vk",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drawer Menu Items (Image 1)
                NavigationDrawerItem(
                    label = { Text("Library", fontWeight = FontWeight.SemiBold) },
                    selected = uiState.selectedTab == LibraryTab.SONGS,
                    onClick = {
                        viewModel.selectTab(LibraryTab.SONGS)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Audiotrack, contentDescription = null) },
                    badge = {
                        Text(
                            text = "${uiState.tracks.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CardSurfaceElevated,
                        selectedTextColor = TextPrimary,
                        unselectedTextColor = TextSecondary,
                        selectedIconColor = NeoViolet,
                        unselectedIconColor = TextTertiary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Listening stats", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onStatsClick()
                    },
                    icon = { Icon(Icons.Default.Insights, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = TextSecondary,
                        unselectedIconColor = NeoCyan
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Most Played (Hall of Fame)", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLeaderboardClick()
                    },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = TextSecondary,
                        unselectedIconColor = HolographicGold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Scan media", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        viewModel.syncLibrary()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                    badge = {
                        if (uiState.isScanning) {
                            Text("Scanning...", color = NeoCyan, fontSize = 11.sp)
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = TextSecondary,
                        unselectedIconColor = NeoLime
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = TextSecondary,
                        unselectedIconColor = TextTertiary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("About", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = TextSecondary,
                        unselectedIconColor = TextTertiary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(OledBlack)
        ) {
            // Ambient Aura
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

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Search Bar with Hamburger Drawer Toggle (Images 3 & 5)
                TopSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSyncClick = { viewModel.syncLibrary() },
                    isScanning = uiState.isScanning
                )

                // Tab Content Area with Smooth Transitions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "tab_content_transition"
                    ) { tab ->
                        when (tab) {
                            LibraryTab.HOME -> {
                                HomeTabContent(
                                    uiState = uiState,
                                    onHistoryClick = { viewModel.playHistory() },
                                    onFavoritesClick = { viewModel.playFavorites() },
                                    onMostPlayedClick = onLeaderboardClick,
                                    onShuffleClick = { viewModel.shuffleAll(uiState.tracks) },
                                    onRediscoverClick = { viewModel.playRediscover() },
                                    onForYouNowClick = { viewModel.playForYouNow() },
                                    onTrackClick = { track -> viewModel.playTrack(track, uiState.tracks) }
                                )
                            }
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
                            LibraryTab.RADIO -> {
                                val radioViewModel: RadioViewModel = hiltViewModel()
                                RadioScreen(viewModel = radioViewModel)
                            }
                        }
                    }
                }

                // BOTTOM NAVIGATION BAR (Images 3 & 5)
                BottomNavigationBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelect = { viewModel.selectTab(it) }
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = CardSurfaceElevated,
            title = {
                Text(
                    text = "Starscript Music",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(text = "by vk · Neo-Groove Dark Pop edition", color = NeoCyan, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "High-fidelity offline music player inspired by Oto Music Plus. Features 100% free Google Drive cloud backup, Media3 ExoPlayer, Room database, and global radio streaming.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = NeoViolet, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Top Search Bar with Hamburger menu button (Image 3 & 5).
 */
@Composable
private fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSyncClick: () -> Unit,
    isScanning: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Drawer Toggle
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(42.dp)
                .bouncyClick(onClick = onMenuClick)
                .clip(CircleShape)
                .background(CardSurface.copy(alpha = 0.8f))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Search Pill
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search your music",
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
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurfaceElevated.copy(alpha = 0.7f),
                unfocusedContainerColor = CardSurface.copy(alpha = 0.6f),
                focusedBorderColor = NeoViolet,
                unfocusedBorderColor = Color(0x22FFFFFF),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = NeoViolet
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Sync / Refresh
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
    }
}

/**
 * Modern Bottom Navigation Bar with labeled icons & spring physics (Images 3 & 5).
 */
@Composable
private fun BottomNavigationBar(
    selectedTab: LibraryTab,
    onTabSelect: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(LibraryTab.HOME, "Home", Icons.Default.Home),
        Triple(LibraryTab.SONGS, "Songs", Icons.Default.Audiotrack),
        Triple(LibraryTab.PLAYLISTS, "Playlists", Icons.Default.QueueMusic),
        Triple(LibraryTab.ARTISTS, "Artists", Icons.Default.Mic),
        Triple(LibraryTab.RADIO, "Radio", Icons.Default.Radio)
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        containerColor = DeepSpaceCharcoal.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        items.forEach { (tab, title, icon) ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = NeoCyan,
                    indicatorColor = NeoViolet.copy(alpha = 0.85f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

/**
 * Home Tab directly inspired by Image 3 & 5:
 * Quick action circles (History, Favorites, Most Played, Shuffle) + Smart Discovery Cards.
 */
@Composable
private fun HomeTabContent(
    uiState: LibraryUiState,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMostPlayedClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRediscoverClick: () -> Unit,
    onForYouNowClick: () -> Unit,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Quick Action Circles Row: History, Favorites, Most Played, Shuffle (Images 3 & 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickCircleAction(
                    label = "History",
                    icon = Icons.Default.History,
                    backgroundColor = Color(0xFF1E3A5F),
                    iconColor = NeoElectricBlue,
                    onClick = onHistoryClick
                )

                QuickCircleAction(
                    label = "Favorites",
                    icon = Icons.Default.Favorite,
                    backgroundColor = Color(0xFF4A154B),
                    iconColor = NeoMagenta,
                    onClick = onFavoritesClick
                )

                QuickCircleAction(
                    label = "Most played",
                    icon = Icons.Default.TrendingUp,
                    backgroundColor = Color(0xFF4A3515),
                    iconColor = HolographicGold,
                    onClick = onMostPlayedClick
                )

                QuickCircleAction(
                    label = "Shuffle",
                    icon = Icons.Default.Shuffle,
                    backgroundColor = Color(0xFF0F3D3E),
                    iconColor = NeoLime,
                    onClick = onShuffleClick
                )
            }
        }

        // Featured Discovery Cards (Images 3 & 5)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // "Rediscover" Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF281C38), Color(0xFF171321))
                            )
                        )
                        .border(1.dp, Color(0x33A855F7), RoundedCornerShape(24.dp))
                        .bouncyClick(onClick = onRediscoverClick)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Graphic Disc Art
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFD8B4FE), Color(0xFF7E22CE))
                                    )
                                )
                                .border(4.dp, Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFC084FC)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FROM YOUR PAST",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC084FC)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rediscover",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextPrimary
                            )
                        }
                    }
                }

                // "For You Now" Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF152A38), Color(0xFF0F1A24))
                            )
                        )
                        .border(1.dp, Color(0x3306B6D4), RoundedCornerShape(24.dp))
                        .bouncyClick(onClick = onForYouNowClick)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Glowing Orb Art
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF67E8F9), Color(0xFF0284C7))
                                    )
                                )
                                .blur(1.dp)
                        )

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TUNED TO RIGHT NOW",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "For You Now",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // "Daily mixes" Header & Section
        item {
            Column {
                Text(
                    text = "Daily mixes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "From your routines",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Daily Mix Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Weekend Evening
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3B1D54), Color(0xFF221133))
                            )
                        )
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
                        .bouncyClick(onClick = onForYouNowClick)
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "Weekend\nEvening",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                }

                // Weekday Morning
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5A2A2A), Color(0xFF331616))
                            )
                        )
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
                        .bouncyClick(onClick = onShuffleClick)
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "Weekday\nMorning",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                }
            }
        }

        // Recently Added Tracks Row
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recently added",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        items(uiState.recentTracks.take(8), key = { it.id }) { track ->
            TrackItemRow(
                track = track,
                isPlaying = track.id == uiState.playbackState.currentTrack?.id,
                onTrackClick = { onTrackClick(track) },
                onFavoriteClick = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun QuickCircleAction(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.bouncyClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, Color(0x22FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondary
        )
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
