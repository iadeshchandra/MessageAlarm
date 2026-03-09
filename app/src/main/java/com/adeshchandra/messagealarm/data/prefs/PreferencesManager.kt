package com.adeshchandra.messagealarm.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "message_alarm_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val MASTER_ENABLED        = booleanPreferencesKey("master_enabled")
        val ALARM_VOLUME          = intPreferencesKey("alarm_volume")
        val VIBRATION_ENABLED     = booleanPreferencesKey("vibration_enabled")
        val SCREEN_WAKE_ENABLED   = booleanPreferencesKey("screen_wake_enabled")
        val ALARM_SOUND_URI       = stringPreferencesKey("alarm_sound_uri")
        val SNOOZE_MINUTES        = intPreferencesKey("snooze_minutes")
        val DND_ENABLED           = booleanPreferencesKey("dnd_enabled")
        val DND_START_HOUR        = intPreferencesKey("dnd_start_hour")
        val DND_END_HOUR          = intPreferencesKey("dnd_end_hour")
        val REPEAT_ALARM          = booleanPreferencesKey("repeat_alarm")
        val REPEAT_INTERVAL_SEC   = intPreferencesKey("repeat_interval_sec")
        val REPEAT_MAX_TIMES      = intPreferencesKey("repeat_max_times")
        val FLASH_ENABLED         = booleanPreferencesKey("flash_enabled")
        val FIRST_LAUNCH          = booleanPreferencesKey("first_launch")
    }

    data class Settings(
        val masterEnabled: Boolean      = true,
        val alarmVolume: Int            = 80,
        val vibrationEnabled: Boolean   = true,
        val screenWakeEnabled: Boolean  = true,
        val alarmSoundUri: String       = "",    // empty = use default ringtone
        val snoozeMinutes: Int          = 5,
        val dndEnabled: Boolean         = false,
        val dndStartHour: Int           = 22,   // 10 PM
        val dndEndHour: Int             = 7,    // 7 AM
        val repeatAlarm: Boolean        = true,
        val repeatIntervalSec: Int      = 30,
        val repeatMaxTimes: Int         = 3,
        val flashEnabled: Boolean       = false,
        val firstLaunch: Boolean        = true
    )

    val settingsFlow: Flow<Settings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            Settings(
                masterEnabled     = prefs[MASTER_ENABLED]       ?: true,
                alarmVolume       = prefs[ALARM_VOLUME]         ?: 80,
                vibrationEnabled  = prefs[VIBRATION_ENABLED]    ?: true,
                screenWakeEnabled = prefs[SCREEN_WAKE_ENABLED]  ?: true,
                alarmSoundUri     = prefs[ALARM_SOUND_URI]      ?: "",
                snoozeMinutes     = prefs[SNOOZE_MINUTES]       ?: 5,
                dndEnabled        = prefs[DND_ENABLED]          ?: false,
                dndStartHour      = prefs[DND_START_HOUR]       ?: 22,
                dndEndHour        = prefs[DND_END_HOUR]         ?: 7,
                repeatAlarm       = prefs[REPEAT_ALARM]         ?: true,
                repeatIntervalSec = prefs[REPEAT_INTERVAL_SEC]  ?: 30,
                repeatMaxTimes    = prefs[REPEAT_MAX_TIMES]     ?: 3,
                flashEnabled      = prefs[FLASH_ENABLED]        ?: false,
                firstLaunch       = prefs[FIRST_LAUNCH]         ?: true
            )
        }

    suspend fun setMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { it[MASTER_ENABLED] = enabled }
    }

    suspend fun setAlarmVolume(volume: Int) {
        context.dataStore.edit { it[ALARM_VOLUME] = volume }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setScreenWakeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SCREEN_WAKE_ENABLED] = enabled }
    }

    suspend fun setAlarmSoundUri(uri: String) {
        context.dataStore.edit { it[ALARM_SOUND_URI] = uri }
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_MINUTES] = minutes }
    }

    suspend fun setDndEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DND_ENABLED] = enabled }
    }

    suspend fun setDndHours(startHour: Int, endHour: Int) {
        context.dataStore.edit {
            it[DND_START_HOUR] = startHour
            it[DND_END_HOUR]   = endHour
        }
    }

    suspend fun setRepeatAlarm(enabled: Boolean) {
        context.dataStore.edit { it[REPEAT_ALARM] = enabled }
    }

    suspend fun setRepeatSettings(intervalSec: Int, maxTimes: Int) {
        context.dataStore.edit {
            it[REPEAT_INTERVAL_SEC] = intervalSec
            it[REPEAT_MAX_TIMES]    = maxTimes
        }
    }

    suspend fun setFlashEnabled(enabled: Boolean) {
        context.dataStore.edit { it[FLASH_ENABLED] = enabled }
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { it[FIRST_LAUNCH] = false }
    }
}
