package com.vk.starscriptmusic.data.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intelligent Sleep Timer with gentle volume fade-out.
 * Automatically dims playback volume over the last 30 seconds before pausing.
 */
@Singleton
class SleepTimerManager @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    fun startTimer(
        durationMinutes: Int,
        onVolumeFade: (Float) -> Unit,
        onTimerComplete: () -> Unit
    ) {
        cancelTimer()
        if (durationMinutes <= 0) return

        val totalSeconds = durationMinutes * 60
        _remainingSeconds.value = totalSeconds
        _isTimerActive.value = true

        timerJob = scope.launch {
            var currentSec = totalSeconds
            while (isActive && currentSec > 0) {
                delay(1000)
                currentSec--
                _remainingSeconds.value = currentSec

                // Volume fade-out during the final 30 seconds
                if (currentSec in 1..30) {
                    val fadeFactor = currentSec / 30.0f
                    onVolumeFade(fadeFactor.coerceIn(0.05f, 1.0f))
                }
            }

            if (currentSec <= 0) {
                onVolumeFade(0.0f)
                onTimerComplete()
                delay(500)
                onVolumeFade(1.0f) // Reset volume for next session
                _isTimerActive.value = false
                _remainingSeconds.value = 0
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerActive.value = false
        _remainingSeconds.value = 0
    }
}
