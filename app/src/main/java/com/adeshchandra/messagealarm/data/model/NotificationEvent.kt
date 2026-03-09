package com.adeshchandra.messagealarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUpworkBestMatch: Boolean = false,
    val isFiverr: Boolean = false,
    val isUpwork: Boolean = false
)
