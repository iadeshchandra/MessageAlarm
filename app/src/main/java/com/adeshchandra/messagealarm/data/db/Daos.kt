package com.adeshchandra.messagealarm.data.db

import androidx.room.*
import com.adeshchandra.messagealarm.data.model.AppConfig
import com.adeshchandra.messagealarm.data.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NotificationEvent): Long

    @Query("SELECT * FROM notification_events ORDER BY timestamp DESC LIMIT 200")
    fun getAllFlow(): Flow<List<NotificationEvent>>

    @Query("SELECT * FROM notification_events ORDER BY timestamp DESC LIMIT 200")
    suspend fun getAll(): List<NotificationEvent>

    @Query("DELETE FROM notification_events")
    suspend fun deleteAll()

    @Query("DELETE FROM notification_events WHERE id NOT IN (SELECT id FROM notification_events ORDER BY timestamp DESC LIMIT 200)")
    suspend fun pruneOld()

    @Query("SELECT COUNT(*) FROM notification_events WHERE timestamp > :since")
    suspend fun countSince(since: Long): Int
}

@Dao
interface AppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AppConfig)

    @Update
    suspend fun update(config: AppConfig)

    @Query("SELECT * FROM app_configs ORDER BY appName ASC")
    fun getAllFlow(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_configs ORDER BY appName ASC")
    suspend fun getAll(): List<AppConfig>

    @Query("SELECT * FROM app_configs WHERE packageName = :pkg LIMIT 1")
    suspend fun getByPackage(pkg: String): AppConfig?

    @Query("SELECT * FROM app_configs WHERE isEnabled = 1")
    suspend fun getEnabled(): List<AppConfig>

    @Query("UPDATE app_configs SET isEnabled = :enabled WHERE packageName = :pkg")
    suspend fun setEnabled(pkg: String, enabled: Boolean)

    @Query("UPDATE app_configs SET upworkBestMatchOnly = :bestMatchOnly WHERE packageName = :pkg")
    suspend fun setUpworkBestMatchOnly(pkg: String, bestMatchOnly: Boolean)

    @Query("DELETE FROM app_configs WHERE packageName = :pkg")
    suspend fun delete(pkg: String)
}
