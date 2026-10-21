package com.vk.starscriptmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.StarscriptMusicTheme
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.ui.artist.ArtistHubScreen
import com.vk.starscriptmusic.ui.artist.ArtistHubViewModel
import com.vk.starscriptmusic.ui.library.LibraryScreen
import com.vk.starscriptmusic.ui.library.LibraryViewModel
import com.vk.starscriptmusic.ui.navigation.Screen
import com.vk.starscriptmusic.ui.playlist.PlaylistDetailScreen
import com.vk.starscriptmusic.ui.playlist.PlaylistDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Starscript Music Single Activity Entry Point.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StarscriptMusicTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAudioPermission = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.READ_MEDIA_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    if (!hasAudioPermission) {
        PermissionRequiredScreen(
            onRequestPermission = {
                val perms = mutableListOf(Manifest.permission.READ_MEDIA_AUDIO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(perms.toTypedArray())
            }
        )
    } else {
        Scaffold(
            containerColor = OledBlack
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Library.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(
                    route = Screen.Library.route,
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }
                ) {
                    val libraryViewModel: LibraryViewModel = hiltViewModel()
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onArtistClick = { artistName ->
                            navController.navigate(Screen.ArtistHub.createRoute(artistName))
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        }
                    )
                }

                composable(
                    route = Screen.ArtistHub.route,
                    arguments = listOf(
                        navArgument("artistName") { type = NavType.StringType }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(350)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(350)
                        )
                    }
                ) {
                    val artistHubViewModel: ArtistHubViewModel = hiltViewModel()
                    ArtistHubScreen(
                        viewModel = artistHubViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument("playlistId") { type = NavType.LongType }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(350)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(350)
                        )
                    }
                ) {
                    val playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel()
                    PlaylistDetailScreen(
                        viewModel = playlistDetailViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ACCESS YOUR MUSIC",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Starscript Music needs permission to scan your local storage and discover your audio library offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = NeoViolet),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Grant Audio Permission",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun fadeIn(animationSpec: androidx.compose.animation.core.DurationBasedAnimationSpec<Float>) =
    androidx.compose.animation.fadeIn(animationSpec)

private fun fadeOut(animationSpec: androidx.compose.animation.core.DurationBasedAnimationSpec<Float>) =
    androidx.compose.animation.fadeOut(animationSpec)
