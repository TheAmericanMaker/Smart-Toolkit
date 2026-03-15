package com.smarttoolkit.app.feature.tallycounter

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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TallyCounterForegroundService : Service() {

    @Inject lateinit var stateHolder: TallyCounterStateHolder

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
                stateHolder.ensureLoaded()
                startForeground(NOTIFICATION_ID, buildNotification())
                observeState()
            }
            ACTION_INCREMENT -> {
                stateHolder.increment()
            }
            ACTION_DECREMENT -> {
                stateHolder.decrement()
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeState() {
        serviceScope.launch {
            stateHolder.uiState.collect { _ ->
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tally Counter",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows current tally count with quick controls"
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val count = stateHolder.uiState.value.count

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_NAVIGATE_TO, "tallycounter")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Tally Counter")
            .setContentText("Count: $count")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setSilent(true)
            .addAction(0, "+1", buildActionIntent(ACTION_INCREMENT))
            .addAction(0, "-1", buildActionIntent(ACTION_DECREMENT))
            .build()
    }

    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, TallyCounterForegroundService::class.java).apply {
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
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.smarttoolkit.app.tally.START"
        const val ACTION_INCREMENT = "com.smarttoolkit.app.tally.INCREMENT"
        const val ACTION_DECREMENT = "com.smarttoolkit.app.tally.DECREMENT"
        const val ACTION_STOP = "com.smarttoolkit.app.tally.STOP"

        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val NOTIFICATION_ID = 4001
        const val CHANNEL_ID = "tally_counter"
    }
}
