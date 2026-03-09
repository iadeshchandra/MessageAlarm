package com.adeshchandra.messagealarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.adeshchandra.messagealarm.data.db.AppDatabase
import com.adeshchandra.messagealarm.data.prefs.PreferencesManager

class MessageAlarmApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesManager = PreferencesManager(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Service running channel (persistent, low importance)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Message Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the notification alarm service running"
                setShowBadge(false)
            }

            // Alarm triggered channel (high importance)
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shown when an alarm is triggered"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannels(listOf(serviceChannel, alarmChannel))
        }
    }

    companion object {
        const val CHANNEL_SERVICE = "ch_service"
        const val CHANNEL_ALARM   = "ch_alarm"

        fun get(app: Application): MessageAlarmApp = app as MessageAlarmApp
    }
}
