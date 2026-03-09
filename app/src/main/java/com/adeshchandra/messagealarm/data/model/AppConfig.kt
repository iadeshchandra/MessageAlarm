package com.adeshchandra.messagealarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configs")
data class AppConfig(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean = false,
    val upworkBestMatchOnly: Boolean = false, // Upwork: alarm only for Best Matches
    val customSound: String? = null           // Per-app custom sound URI
)

// Well-known packages for special handling
object KnownApps {
    const val FIVERR       = "com.fiverr.fiverr"
    const val UPWORK       = "com.upwork.android.apps.main"
    const val WHATSAPP     = "com.whatsapp"
    const val TELEGRAM     = "org.telegram.messenger"
    const val GMAIL        = "com.google.android.gm"
    const val MESSENGER    = "com.facebook.orca"

    val UPWORK_BEST_MATCH_KEYWORDS = listOf(
        "best match", "best matches", "job match", "job matches",
        "new job", "jobs for you", "matches your profile",
        "recommended job", "top job"
    )

    val FIVERR_ALERT_KEYWORDS = listOf(
        "new message", "new order", "order placed", "new request",
        "buyer request", "you got a new", "new notification",
        "offer accepted", "new inquiry"
    )
}
