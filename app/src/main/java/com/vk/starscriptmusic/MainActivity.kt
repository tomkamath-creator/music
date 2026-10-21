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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vk.starscriptmusic.core.theme.CardSurface
import com.vk.starscriptmusic.core.theme.CardSurfaceElevated
import com.vk.starscriptmusic.core.theme.DeepSpaceCharcoal
import com.vk.starscriptmusic.core.theme.HolographicGold
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoMagenta
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import com.vk.starscriptmusic.core.theme.StarscriptMusicTheme
import com.vk.starscriptmusic.core.theme.TextMuted
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
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
                MainRootScreen()
            }
        }
    }
}

@Composable
fun MainRootScreen() {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAudioPermission = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: hasAudioPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.READ_MEDIA_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    val infiniteTransition = rememberInfiniteTransition(label = "halo_breathing")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Scaffold(
        containerColor = OledBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OledBlack, DeepSpaceCharcoal, OledBlack)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Neo-Groove Halo Back-Glow
            Box(
                modifier = Modifier
                    .size(280.dp * haloPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeoViolet.copy(alpha = 0.35f),
                                NeoMagenta.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(64.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Signature Halo Pill for App Branding
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardSurface.copy(alpha = 0.85f))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(NeoViolet, NeoMagenta, NeoCyan)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STARSCRIPT MUSIC",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "by vk",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                color = NeoCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Phase 1 Architecture Verification Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceElevated.copy(alpha = 0.7f))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeoViolet,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Media3 Audio Engine",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            PillStatus(label = "INITIALIZED", color = NeoCyan)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = NeoMagenta,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Room Cache & KSP",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            PillStatus(label = "ACTIVE", color = NeoMagenta)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = HolographicGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Audio Permission",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (hasAudioPermission) {
                                PillStatus(label = "GRANTED", color = HolographicGold)
                            } else {
                                PillStatus(label = "PENDING", color = TextMuted)
                            }
                        }
                    }
                }

                if (!hasAudioPermission) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val perms = mutableListOf(Manifest.permission.READ_MEDIA_AUDIO)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeoViolet
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Grant Audio Permission",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Phase 1 Foundation Complete • Ready for Phase 2 Scanner",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PillStatus(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
