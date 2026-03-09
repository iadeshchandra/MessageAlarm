package com.adeshchandra.messagealarm.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.R
import com.adeshchandra.messagealarm.alarm.AlarmService
import com.adeshchandra.messagealarm.data.db.AppDatabase
import com.adeshchandra.messagealarm.data.model.AppConfig
import com.adeshchandra.messagealarm.data.model.KnownApps
import com.adeshchandra.messagealarm.data.model.NotificationEvent
import com.adeshchandra.messagealarm.data.prefs.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class NotificationAlarmService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var prefs: PreferencesManager
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(applicationContext)
        db = AppDatabase.getInstance(applicationContext)
        showPersistentNotification()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg == packageName) return  // Ignore our own notifications
        if (sbn.isOngoing) return       // Ignore ongoing notifications (music players, etc.)

        scope.launch {
            val settings = prefs.settingsFlow.first()

            // Global kill switch
            if (!settings.masterEnabled) return@launch

            // Do Not Disturb check
            if (settings.dndEnabled && isInDndWindow(settings.dndStartHour, settings.dndEndHour)) return@launch

            // Get app config
            val appConfig = db.appConfigDao().getByPackage(pkg) ?: return@launch
            if (!appConfig.isEnabled) return@launch

            // Extract notification content
            val extras = sbn.notification.extras
            val title   = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val content = (extras.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))?.toString()?.trim() ?: ""

            if (title.isBlank() && content.isBlank()) return@launch

            val isUpwork      = pkg == KnownApps.UPWORK
            val isFiverr      = pkg == KnownApps.FIVERR
            val isUpworkBM    = isUpwork && isUpworkBestMatch(title, content)

            // Upwork Best Match Only mode
            if (isUpwork && appConfig.upworkBestMatchOnly && !isUpworkBM) return@launch

            // Save to history
            val event = NotificationEvent(
                packageName      = pkg,
                appName          = appConfig.appName,
                title            = title,
                content          = content,
                isUpworkBestMatch= isUpworkBM,
                isFiverr         = isFiverr,
                isUpwork         = isUpwork
            )
            db.notificationDao().insert(event)
            db.notificationDao().pruneOld()

            // Trigger alarm on main thread
            withContext(Dispatchers.Main) {
                triggerAlarm(
                    appName     = appConfig.appName,
                    title       = title,
                    content     = content,
                    pkg         = pkg,
                    settings    = settings,
                    soundUri    = appConfig.customSound ?: settings.alarmSoundUri,
                    isBestMatch = isUpworkBM
                )
            }
        }
    }

    private fun isUpworkBestMatch(title: String, content: String): Boolean {
        val combined = "${title.lowercase()} ${content.lowercase()}"
        return KnownApps.UPWORK_BEST_MATCH_KEYWORDS.any { kw -> combined.contains(kw) }
    }

    private fun isInDndWindow(startHour: Int, endHour: Int): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (startHour > endHour) {
            // Crosses midnight e.g. 22:00 - 07:00
            hour >= startHour || hour < endHour
        } else {
            hour in startHour until endHour
        }
    }

    private fun triggerAlarm(
        appName: String,
        title: String,
        content: String,
        pkg: String,
        settings: PreferencesManager.Settings,
        soundUri: String,
        isBestMatch: Boolean
    ) {
        val intent = AlarmService.buildStartIntent(
            context    = applicationContext,
            appName    = appName,
            title      = title,
            content    = content,
            pkg        = pkg,
            volume     = settings.alarmVolume,
            vibrate    = settings.vibrationEnabled,
            soundUri   = soundUri,
            repeat     = settings.repeatAlarm,
            maxTimes   = settings.repeatMaxTimes,
            isBestMatch= isBestMatch
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showPersistentNotification() {
        val notification = NotificationCompat.Builder(this, MessageAlarmApp.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(getString(R.string.service_running_title))
            .setContentText(getString(R.string.service_running_desc))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
        startForeground(SERVICE_NOTIF_ID, notification)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Service connected successfully
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Attempt to reconnect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationAlarmService::class.java))
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val SERVICE_NOTIF_ID = 1000

        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledPackages = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return enabledPackages.contains(context.packageName)
        }
    }
}
