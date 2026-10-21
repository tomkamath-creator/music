package com.vk.starscriptmusic.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.vk.starscriptmusic.core.theme.TextPrimary
import com.vk.starscriptmusic.core.theme.TextSecondary
import com.vk.starscriptmusic.core.theme.TextTertiary
import com.vk.starscriptmusic.ui.components.GlassCard
import com.vk.starscriptmusic.ui.components.bouncyClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings & System screen for Starscript Music.
 * Features crossfade configuration, sleep timer with volume fade-out,
 * zero-cost Google Drive / local backup, and app attribution.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OledBlack, DeepSpaceCharcoal, OledBlack)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Top Header Bar
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
                            text = "SETTINGS & AUDIO",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = "SYSTEM PREFERENCES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeoCyan
                        )
                    }
                }
            }

            // Crossfade Configuration Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = NeoViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Crossfade Transitions",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${uiState.crossfadeSeconds}s",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = NeoViolet
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Seamless audio blending between consecutive tracks (0–8s).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Slider(
                                value = uiState.crossfadeSeconds.toFloat(),
                                onValueChange = { viewModel.setCrossfade(it.toInt()) },
                                valueRange = 0f..8f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeoViolet,
                                    activeTrackColor = NeoViolet,
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Sleep Timer Card with Volume Fade-Out
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = NeoCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Sleep Timer",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (uiState.isSleepTimerActive) {
                                    val minutes = uiState.sleepTimerRemainingSeconds / 60
                                    val seconds = uiState.sleepTimerRemainingSeconds % 60
                                    Text(
                                        text = String.format(Locale.US, "%d:%02d", minutes, seconds),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = NeoCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Playback pauses automatically with a gentle 30-second volume fade-out.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Timer Duration Pills
                            val timerOptions = listOf(
                                0 to "Off",
                                15 to "15m",
                                30 to "30m",
                                45 to "45m",
                                60 to "60m"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                timerOptions.forEach { (minutes, label) ->
                                    val isSelected = if (minutes == 0) {
                                        !uiState.isSleepTimerActive
                                    } else {
                                        uiState.isSleepTimerActive && (uiState.sleepTimerRemainingSeconds in (minutes * 60 - 59)..(minutes * 60))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .bouncyClick { viewModel.setSleepTimer(minutes) }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) NeoCyan.copy(alpha = 0.2f) else CardSurfaceElevated)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) NeoCyan else Color(0x1AFFFFFF),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) NeoCyan else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Zero-Cost Google Drive Cloud & Offline Backup Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = NeoMagenta,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Zero-Cost Cloud Backup",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Safely backs up playlists, favorites, and Top 50 play counts to your personal hidden Google Drive appDataFolder or private local storage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            if (uiState.lastBackupTimestamp > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                                Text(
                                    text = "Last Backup: ${dateFormat.format(Date(uiState.lastBackupTimestamp))}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = NeoMagenta
                                )
                            }

                            if (uiState.backupMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.backupMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeoCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.performBackup() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeoMagenta),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Backup Now",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.performRestore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Restore",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Library Scanner Resync Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rescan Audio Library",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Re-indexes device audio files and auto-generates artist hubs.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Button(
                                onClick = { viewModel.rescanLibrary() },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = NeoCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // About & Production Attribution Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderBrush = Brush.linearGradient(
                            colors = listOf(NeoViolet, NeoCyan)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                    letterSpacing = 2.sp,
                                    color = NeoCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Neo-Groove Dark Pop Audio Player\nOffline-First • Zero Paid APIs • Android 14+ (minSdk 34)\nJetpack Media3 • Room & KSP • Compose",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Version 1.0.0 (Production Release)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
