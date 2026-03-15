package com.smarttoolkit.app.feature.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.smarttoolkit.app.MainActivity
import com.smarttoolkit.app.R
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerForegroundService : Service() {

    @Inject lateinit var stateHolder: TimerStateHolder
    @Inject lateinit var prefs: UserPreferencesRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var countdownJob: Job? = null
    private var activeRingtone: Ringtone? = null
    private var autoDismissJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)
                val alarmUri = intent.getStringExtra(EXTRA_ALARM_URI) ?: ""
                if (durationMs > 0) {
                    startTimer(durationMs, alarmUri)
                }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_CANCEL -> cancelTimer()
            ACTION_DISMISS -> dismissAlarm()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationMs: Long, alarmUri: String) {
        stateHolder.start(durationMs)
        startForeground(COUNTDOWN_NOTIFICATION_ID, buildCountdownNotification(durationMs, isRunning = true))
        startCountdownLoop(alarmUri)
    }

    private fun startCountdownLoop(alarmUri: String) {
        countdownJob?.cancel()
        countdownJob = serviceScope.launch {
            var lastNotificationUpdate = 0L
            while (true) {
                val finished = stateHolder.tick()
                if (finished) {
                    onTimerFinished(alarmUri)
                    break
                }

                val state = stateHolder.timerState.value
                if (!state.isRunning) break

                // Update notification every ~1 second
                val now = System.currentTimeMillis()
                if (now - lastNotificationUpdate >= 1000) {
                    lastNotificationUpdate = now
                    notificationManager.notify(
                        COUNTDOWN_NOTIFICATION_ID,
                        buildCountdownNotification(state.remainingMs, isRunning = true)
                    )
                }
                delay(100)
            }
        }
    }

    private fun pauseTimer() {
        countdownJob?.cancel()
        stateHolder.pause()
        val state = stateHolder.timerState.value
        notificationManager.notify(
            COUNTDOWN_NOTIFICATION_ID,
            buildCountdownNotification(state.remainingMs, isRunning = false)
        )
    }

    private fun resumeTimer() {
        stateHolder.resume()
        // Need to read the alarm URI for the countdown loop
        serviceScope.launch {
            val alarmUri = prefs.timerAlarmSound.first()
            startCountdownLoop(alarmUri)
        }
        val state = stateHolder.timerState.value
        notificationManager.notify(
            COUNTDOWN_NOTIFICATION_ID,
            buildCountdownNotification(state.remainingMs, isRunning = true)
        )
    }

    private fun cancelTimer() {
        countdownJob?.cancel()
        autoDismissJob?.cancel()
        stopAlarmPlayback()
        serviceScope.launch {
            val h = prefs.timerHours.first()
            val m = prefs.timerMinutes.first()
            val s = prefs.timerSeconds.first()
            stateHolder.cancel(h, m, s)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(COUNTDOWN_NOTIFICATION_ID)
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        stopSelf()
    }

    private fun dismissAlarm() {
        countdownJob?.cancel()
        autoDismissJob?.cancel()
        stopAlarmPlayback()
        serviceScope.launch {
            val h = prefs.timerHours.first()
            val m = prefs.timerMinutes.first()
            val s = prefs.timerSeconds.first()
            stateHolder.dismiss(h, m, s)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(COUNTDOWN_NOTIFICATION_ID)
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        stopSelf()
    }

    private fun onTimerFinished(alarmUri: String) {
        // Remove the countdown notification
        notificationManager.cancel(COUNTDOWN_NOTIFICATION_ID)

        // Play alarm
        playAlarm(alarmUri)

        // Show alarm notification with Dismiss action
        notificationManager.notify(ALARM_NOTIFICATION_ID, buildAlarmNotification())

        // Auto-dismiss after 5 minutes
        autoDismissJob = serviceScope.launch {
            delay(5 * 60 * 1000L)
            dismissAlarm()
        }
    }

    private fun playAlarm(alarmUri: String) {
        try {
            val uri = if (alarmUri.isNotEmpty()) {
                Uri.parse(alarmUri)
            } else {
                // Fall back to selected sound in state
                val state = stateHolder.timerState.value
                if (state.availableSounds.isNotEmpty() && state.selectedSoundIndex in state.availableSounds.indices) {
                    state.availableSounds[state.selectedSoundIndex].uri
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }
            }
            activeRingtone = RingtoneManager.getRingtone(this, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activeRingtone?.isLooping = true
            }
            activeRingtone?.play()
        } catch (_: Exception) {}

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } catch (_: Exception) {}
    }

    private fun stopAlarmPlayback() {
        try {
            activeRingtone?.stop()
        } catch (_: Exception) {}
        activeRingtone = null

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.cancel()
        } catch (_: Exception) {}
    }

    // --- Notification builders ---

    private fun createNotificationChannels() {
        val countdown = NotificationChannel(
            CHANNEL_COUNTDOWN,
            "Timer Countdown",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows remaining time while timer is running"
            setSound(null, null)
        }

        val alarm = NotificationChannel(
            CHANNEL_ALARM,
            "Timer Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when timer finishes"
            setSound(null, null) // App plays its own ringtone
        }

        notificationManager.createNotificationChannel(countdown)
        notificationManager.createNotificationChannel(alarm)
    }

    private fun buildCountdownNotification(remainingMs: Long, isRunning: Boolean): Notification {
        val timeText = formatTime(remainingMs)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_NAVIGATE_TO, "timer")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_COUNTDOWN)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Timer")
            .setContentText("$timeText remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setSilent(true)

        if (isRunning) {
            builder.addAction(0, "Pause", buildActionIntent(ACTION_PAUSE))
        } else {
            builder.addAction(0, "Resume", buildActionIntent(ACTION_RESUME))
        }
        builder.addAction(0, "Cancel", buildActionIntent(ACTION_CANCEL))

        return builder.build()
    }

    private fun buildAlarmNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_NAVIGATE_TO, "timer")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ALARM)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Time's Up!")
            .setContentText("Tap Dismiss to stop the alarm")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentIntent)
            .addAction(0, "Dismiss", buildActionIntent(ACTION_DISMISS))
            .build()
    }

    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
        autoDismissJob?.cancel()
        stopAlarmPlayback()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.smarttoolkit.app.timer.START"
        const val ACTION_PAUSE = "com.smarttoolkit.app.timer.PAUSE"
        const val ACTION_RESUME = "com.smarttoolkit.app.timer.RESUME"
        const val ACTION_CANCEL = "com.smarttoolkit.app.timer.CANCEL"
        const val ACTION_DISMISS = "com.smarttoolkit.app.timer.DISMISS"

        const val EXTRA_DURATION_MS = "duration_ms"
        const val EXTRA_ALARM_URI = "alarm_uri"
        const val EXTRA_NAVIGATE_TO = "navigate_to"

        const val COUNTDOWN_NOTIFICATION_ID = 1001
        const val ALARM_NOTIFICATION_ID = 1002

        const val CHANNEL_COUNTDOWN = "timer_countdown"
        const val CHANNEL_ALARM = "timer_alarm"
    }
}
