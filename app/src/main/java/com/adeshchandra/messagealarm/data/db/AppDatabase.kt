package com.adeshchandra.messagealarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adeshchandra.messagealarm.data.model.AppConfig
import com.adeshchandra.messagealarm.data.model.NotificationEvent

@Database(
    entities = [NotificationEvent::class, AppConfig::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "message_alarm.db"
                ).build().also { INSTANCE = it }
            }
    }
}
