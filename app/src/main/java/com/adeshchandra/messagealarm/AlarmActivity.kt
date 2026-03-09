package com.adeshchandra.messagealarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.adeshchandra.messagealarm.alarm.AlarmService
import com.adeshchandra.messagealarm.alarm.SnoozeAlarmReceiver
import com.adeshchandra.messagealarm.data.model.KnownApps
import com.adeshchandra.messagealarm.databinding.ActivityAlarmBinding
import com.adeshchandra.messagealarm.data.prefs.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    companion object {
        const val EXTRA_APP_NAME      = "extra_app_name"
        const val EXTRA_TITLE         = "extra_title"
        const val EXTRA_CONTENT       = "extra_content"
        const val EXTRA_PKG           = "extra_pkg"
        const val EXTRA_IS_BEST_MATCH = "extra_is_best_match"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen & show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appName     = intent.getStringExtra(EXTRA_APP_NAME) ?: "Unknown App"
        val title       = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val content     = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val pkg         = intent.getStringExtra(EXTRA_PKG) ?: ""
        val isBestMatch = intent.getBooleanExtra(EXTRA_IS_BEST_MATCH, false)

        bindData(appName, title, content, pkg, isBestMatch)
        bindButtons(appName, title, content)
    }

    private fun bindData(appName: String, title: String, content: String, pkg: String, isBestMatch: Boolean) {
        binding.tvAppName.text = appName
        binding.tvTitle.text = title.ifBlank { appName }
        binding.tvContent.text = content.ifBlank { "New notification received" }

        // Show Best Match badge for Upwork
        binding.chipBestMatch.visibility = if (isBestMatch) View.VISIBLE else View.GONE

        // App-specific icon tint
        when (pkg) {
            KnownApps.UPWORK  -> binding.ivAppIcon.setImageResource(R.drawable.ic_upwork)
            KnownApps.FIVERR  -> binding.ivAppIcon.setImageResource(R.drawable.ic_fiverr)
            else              -> binding.ivAppIcon.setImageResource(R.drawable.ic_notification_bell)
        }
    }

    private fun bindButtons(appName: String, title: String, content: String) {
        // Stop button
        binding.btnStop.setOnClickListener {
            stopAlarmService()
            finish()
        }

        // Snooze button - read snooze minutes from prefs
        binding.btnSnooze.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = PreferencesManager(applicationContext)
                val settings = prefs.settingsFlow.first()
                snoozeAlarm(settings.snoozeMinutes)
                finish()
            }
        }
    }

    private fun stopAlarmService() {
        stopService(Intent(this, AlarmService::class.java))
    }

    private fun snoozeAlarm(snoozeMinutes: Int) {
        stopAlarmService()
        val snoozeIntent = Intent(this, SnoozeAlarmReceiver::class.java).apply {
            putExtra(SnoozeAlarmReceiver.EXTRA_SNOOZE_MIN, snoozeMinutes)
        }
        sendBroadcast(snoozeIntent)
    }

    override fun onBackPressed() {
        // Don't allow back press - user must explicitly stop or snooze
    }
}
