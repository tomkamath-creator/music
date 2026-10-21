package com.vk.starscriptmusic.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.starscriptmusic.core.common.Constants
import com.vk.starscriptmusic.data.local.backup.GoogleDriveBackupManager
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import com.vk.starscriptmusic.domain.usecase.ScanMediaStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val crossfadeSeconds: Int = Constants.DEFAULT_CROSSFADE_SECONDS,
    val sleepTimerRemainingSeconds: Int = 0,
    val isSleepTimerActive: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val backupMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val playbackRepository: PlaybackRepository,
    private val backupManager: GoogleDriveBackupManager,
    private val scanMediaStoreUseCase: ScanMediaStoreUseCase
) : ViewModel() {

    private val crossfadeKey = intPreferencesKey("crossfade_seconds")
    private val _backupMessage = MutableStateFlow<String?>(null)

    private val crossfadeFlow = dataStore.data.map { prefs ->
        prefs[crossfadeKey] ?: Constants.DEFAULT_CROSSFADE_SECONDS
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        crossfadeFlow,
        playbackRepository.sleepTimerRemainingSeconds,
        playbackRepository.isSleepTimerActive,
        backupManager.lastBackupTimestampFlow,
        _backupMessage
    ) { crossfade, sleepRemaining, isSleepActive, lastBackup, message ->
        SettingsUiState(
            crossfadeSeconds = crossfade,
            sleepTimerRemainingSeconds = sleepRemaining,
            isSleepTimerActive = isSleepActive,
            lastBackupTimestamp = lastBackup,
            backupMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setCrossfade(seconds: Int) {
        viewModelScope.launch {
            val clamped = seconds.coerceIn(0, Constants.MAX_CROSSFADE_SECONDS)
            dataStore.edit { prefs ->
                prefs[crossfadeKey] = clamped
            }
            playbackRepository.setCrossfade(clamped)
        }
    }

    fun setSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            playbackRepository.cancelSleepTimer()
        } else {
            playbackRepository.startSleepTimer(minutes)
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            _backupMessage.value = "Backing up..."
            val success = backupManager.performLocalBackup()
            _backupMessage.value = if (success) "Backup saved successfully!" else "Backup failed."
        }
    }

    fun performRestore() {
        viewModelScope.launch {
            _backupMessage.value = "Restoring..."
            val success = backupManager.performLocalRestore()
            _backupMessage.value = if (success) "Restore completed successfully!" else "Restore failed: no backup found."
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _backupMessage.value = "Scanning library..."
            scanMediaStoreUseCase()
            _backupMessage.value = "Library updated!"
        }
    }
}
