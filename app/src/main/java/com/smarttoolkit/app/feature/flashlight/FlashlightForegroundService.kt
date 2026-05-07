package com.smarttoolkit.app.feature.flashlight

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FlashlightForegroundService : Service() {

    @Inject lateinit var stateHolder: FlashlightStateHolder

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
                startForeground(NOTIFICATION_ID, buildNotification())
                observeState()
            }
            ACTION_TOGGLE -> {
                stateHolder.toggle()
                if (!stateHolder.uiState.value.isOn) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    notificationManager.cancel(NOTIFICATION_ID)
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stateHolder.turnOff()
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeState() {
        serviceScope.launch {
            stateHolder.uiState.collect { state ->
                if (state.isOn) {
                    notificationManager.notify(NOTIFICATION_ID, buildNotification())
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    notificationManager.cancel(NOTIFICATION_ID)
                    stopSelf()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Flashlight",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when flashlight is active"
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val state = stateHolder.uiState.value
        val modeText = when (state.mode) {
            FlashMode.STEADY -> "Steady"
            FlashMode.SOS -> "SOS"
            FlashMode.STROBE -> "Strobe"
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_NAVIGATE_TO, "flashlight")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Flashlight")
            .setContentText("$modeText — On")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setSilent(true)
            .addAction(0, "Turn Off", buildActionIntent(ACTION_STOP))
            .build()
    }

    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, FlashlightForegroundService::class.java).apply {
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
        const val ACTION_START = "com.smarttoolkit.app.flashlight.START"
        const val ACTION_TOGGLE = "com.smarttoolkit.app.flashlight.TOGGLE"
        const val ACTION_STOP = "com.smarttoolkit.app.flashlight.STOP"

        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val NOTIFICATION_ID = 3001
        const val CHANNEL_ID = "flashlight_active"
    }
}
