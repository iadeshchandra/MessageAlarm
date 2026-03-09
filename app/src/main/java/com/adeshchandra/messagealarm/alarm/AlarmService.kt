package com.adeshchandra.messagealarm.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.adeshchandra.messagealarm.AlarmActivity
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.R

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var repeatCount = 0
    private var maxRepeat = 3

    companion object {
        const val ACTION_START  = "ACTION_START_ALARM"
        const val ACTION_STOP   = "ACTION_STOP_ALARM"

        const val EXTRA_APP_NAME  = "extra_app_name"
        const val EXTRA_TITLE     = "extra_title"
        const val EXTRA_CONTENT   = "extra_content"
        const val EXTRA_PKG       = "extra_pkg"
        const val EXTRA_VOLUME    = "extra_volume"
        const val EXTRA_VIBRATE   = "extra_vibrate"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_REPEAT    = "extra_repeat"
        const val EXTRA_MAX_TIMES = "extra_max_times"
        const val EXTRA_IS_BM     = "extra_is_best_match"

        fun buildStartIntent(
            context: Context,
            appName: String,
            title: String,
            content: String,
            pkg: String,
            volume: Int,
            vibrate: Boolean,
            soundUri: String,
            repeat: Boolean,
            maxTimes: Int,
            isBestMatch: Boolean = false
        ) = Intent(context, AlarmService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_APP_NAME, appName)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_CONTENT, content)
            putExtra(EXTRA_PKG, pkg)
            putExtra(EXTRA_VOLUME, volume)
            putExtra(EXTRA_VIBRATE, vibrate)
            putExtra(EXTRA_SOUND_URI, soundUri)
            putExtra(EXTRA_REPEAT, repeat)
            putExtra(EXTRA_MAX_TIMES, maxTimes)
            putExtra(EXTRA_IS_BM, isBestMatch)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val appName    = intent.getStringExtra(EXTRA_APP_NAME) ?: "Unknown"
                val title      = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val content    = intent.getStringExtra(EXTRA_CONTENT) ?: ""
                val pkg        = intent.getStringExtra(EXTRA_PKG) ?: ""
                val volume     = intent.getIntExtra(EXTRA_VOLUME, 80)
                val vibrate    = intent.getBooleanExtra(EXTRA_VIBRATE, true)
                val soundUri   = intent.getStringExtra(EXTRA_SOUND_URI) ?: ""
                val repeat     = intent.getBooleanExtra(EXTRA_REPEAT, true)
                val isBestMatch= intent.getBooleanExtra(EXTRA_IS_BM, false)
                maxRepeat      = intent.getIntExtra(EXTRA_MAX_TIMES, 3)

                startForeground(NOTIF_ID, buildForegroundNotification(appName, title))
                launchAlarmActivity(appName, title, content, pkg, isBestMatch)
                playAlarm(soundUri, volume, vibrate, repeat)
            }
            ACTION_STOP -> stopAlarm()
        }
        return START_NOT_STICKY
    }

    private fun launchAlarmActivity(appName: String, title: String, content: String, pkg: String, isBestMatch: Boolean) {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmActivity.EXTRA_APP_NAME, appName)
            putExtra(AlarmActivity.EXTRA_TITLE, title)
            putExtra(AlarmActivity.EXTRA_CONTENT, content)
            putExtra(AlarmActivity.EXTRA_PKG, pkg)
            putExtra(AlarmActivity.EXTRA_IS_BEST_MATCH, isBestMatch)
        }
        startActivity(intent)
    }

    private fun playAlarm(soundUri: String, volume: Int, vibrate: Boolean, repeat: Boolean) {
        try {
            val uri: Uri = if (soundUri.isNotEmpty()) {
                Uri.parse(soundUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(applicationContext, uri)
                isLooping = false
                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val targetVol = (volume / 100f * maxVol).toInt().coerceIn(0, maxVol)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)
                prepare()
                if (repeat) {
                    setOnCompletionListener {
                        repeatCount++
                        if (repeatCount < maxRepeat) {
                            seekTo(0)
                            start()
                        } else {
                            stopAlarm()
                        }
                    }
                }
                start()
            }
        } catch (_: Exception) { }

        if (vibrate) {
            vibrate()
        }
    }

    private fun vibrate() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 500, 300, 500, 300, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        try { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null } catch (_: Exception) {}
        try { vibrator?.cancel() } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildForegroundNotification(appName: String, title: String): Notification {
        val stopIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(this, StopAlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, MessageAlarmApp.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle("🔔 Alarm: $appName")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_stop, "Stop Alarm", stopIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    override fun onDestroy() {
        try { mediaPlayer?.release() } catch (_: Exception) {}
        try { vibrator?.cancel() } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 1001
    }
}
