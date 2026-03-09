package com.adeshchandra.messagealarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.stopService(Intent(context, AlarmService::class.java))
    }
}

class SnoozeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Stop the current alarm
        context.stopService(Intent(context, AlarmService::class.java))

        // Restart after snooze minutes
        val snoozeMs = (intent?.getIntExtra(EXTRA_SNOOZE_MIN, 5) ?: 5) * 60 * 1000L
        val restartIntent = intent?.clone() as? Intent ?: return
        restartIntent.action = AlarmService.ACTION_START
        restartIntent.setClass(context, AlarmService::class.java)

        val pendingIntent = PendingIntent.getService(
            context, SNOOZE_REQUEST_CODE, restartIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + snoozeMs,
            pendingIntent
        )
    }

    companion object {
        const val EXTRA_SNOOZE_MIN   = "extra_snooze_min"
        const val SNOOZE_REQUEST_CODE = 2001
    }
}
