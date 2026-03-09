package com.adeshchandra.messagealarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // The NotificationListenerService is automatically restarted by Android after boot
        // if it was running before. This receiver ensures the app is ready.
        // Nothing explicit needed as the system handles NLS restart.
    }
}
