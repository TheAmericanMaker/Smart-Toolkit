package com.smarttoolkit.app.feature.stopwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarttoolkit.app.MainActivity
import com.smarttoolkit.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StopwatchForegroundService : Service() {

    @Inject lateinit var stateHolder: StopwatchStateHolder

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                stateHolder.start()
                startForeground(NOTIFICATION_ID, buildNotification(isRunning = true))
                startTickLoop()
            }
            ACTION_PAUSE -> {
                stateHolder.pause()
                tickJob?.cancel()
                notificationManager.notify(NOTIFICATION_ID, buildNotification(isRunning = false))
            }
            ACTION_RESUME -> {
                stateHolder.start()
                startTickLoop()
                notificationManager.notify(NOTIFICATION_ID, buildNotification(isRunning = true))
            }
            ACTION_LAP -> {
                stateHolder.lap()
            }
            ACTION_STOP -> {
                stateHolder.reset()
                tickJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            var lastNotificationUpdate = 0L
            while (true) {
                stateHolder.tick()
                val now = System.currentTimeMillis()
                if (now - lastNotificationUpdate >= 1000) {
                    lastNotificationUpdate = now
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(isRunning = true))
                }
                delay(16)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Stopwatch",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows elapsed time while stopwatch is running"
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(isRunning: Boolean): Notification {
        val timeText = formatTime(stateHolder.uiState.value.elapsedMs)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_NAVIGATE_TO, "stopwatch")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Stopwatch")
            .setContentText(timeText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setSilent(true)

        if (isRunning) {
            builder.addAction(0, "Pause", buildActionIntent(ACTION_PAUSE))
            builder.addAction(0, "Lap", buildActionIntent(ACTION_LAP))
        } else {
            builder.addAction(0, "Resume", buildActionIntent(ACTION_RESUME))
            builder.addAction(0, "Stop", buildActionIntent(ACTION_STOP))
        }

        return builder.build()
    }

    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, StopwatchForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        tickJob?.cancel()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.smarttoolkit.app.stopwatch.START"
        const val ACTION_PAUSE = "com.smarttoolkit.app.stopwatch.PAUSE"
        const val ACTION_RESUME = "com.smarttoolkit.app.stopwatch.RESUME"
        const val ACTION_LAP = "com.smarttoolkit.app.stopwatch.LAP"
        const val ACTION_STOP = "com.smarttoolkit.app.stopwatch.STOP"

        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "stopwatch_tracking"
    }
}
