# Keep Room entities
-keep class com.adeshchandra.messagealarm.data.model.** { *; }

# Keep Room DAOs
-keep interface com.adeshchandra.messagealarm.data.db.** { *; }

# Keep NotificationListenerService
-keep class com.adeshchandra.messagealarm.service.NotificationAlarmService { *; }

# Keep AlarmService
-keep class com.adeshchandra.messagealarm.alarm.** { *; }

# Keep BroadcastReceivers
-keep class com.adeshchandra.messagealarm.service.BootReceiver { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# AndroidX Navigation
-keepnames class androidx.navigation.fragment.NavHostFragment

# Suppress warnings
-dontwarn java.lang.invoke.StringConcatFactory
