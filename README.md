Message Alarm is the ultimate notification alarm app for Android, built from the ground up
for freelancers, remote workers, and anyone who cannot afford to miss a single important
notification. In a world where opportunities come and go in seconds — a new Fiverr order,
an Upwork Best Match job, a client reply — a silent notification badge is simply not enough.
Message Alarm changes that completely.

When a notification arrives from any app you choose, Message Alarm does not just buzz once
and disappear. It fires a full-screen alarm popup over your lock screen, plays a loud alarm
tone at maximum volume, vibrates your phone in a strong pattern, and keeps repeating until
you acknowledge it. You will never sleep through a new order again.


⭐ BUILT FOR FIVERR & UPWORK FREELANCERS

Message Alarm is designed with freelancers in mind. It has deep, built-in support for both
Fiverr and Upwork right out of the box.

For Fiverr users, the app detects new messages, new orders, buyer requests, offer
acceptances, and new inquiries — and triggers an immediate alarm so you can respond
faster than your competitors.

For Upwork users, the app goes even further. It can detect when a new Best Match job
appears in your feed — the jobs Upwork specifically recommends for your profile — and
trigger a dedicated alarm with a special Best Match badge on the popup. You can even
enable "Best Matches Only" mode so the app only alarms for those high-priority job
recommendations and ignores everything else. And if you want to stop the Best Match
alerts entirely, one tap turns it off.


🔔 HOW IT WORKS

Message Alarm runs a lightweight background service using Android's official
NotificationListenerService API — the same trusted API used by apps like Tasker and
Android Auto. This means it reads your notifications without needing any dangerous
permissions like SMS access, Contacts, Camera, or Location.

When a notification arrives from an app you have enabled, the service analyses the
content, checks your rules (Do Not Disturb schedule, Best Match only mode, etc.), logs
it to your history, and immediately fires the alarm. The whole process happens in
milliseconds.


📱 FULL-SCREEN ALARM POPUP

When the alarm triggers, a full-screen popup appears directly over your lock screen —
no need to unlock your phone. The popup shows you the app name and icon, the
notification title and full message content, a special Best Match badge for Upwork job
alerts, a STOP button to silence the alarm instantly, and a SNOOZE button to remind you
again in 5, 10, 15, or 30 minutes. The alarm can also repeat itself automatically up to
a set number of times so that even if you are away from your phone, you will come back
to see exactly what you missed and when.


⚙️ POWERFUL CUSTOMISATION

Message Alarm gives you full control over how and when it alarms:

Alarm Sound — choose any ringtone on your phone as the alarm sound, or keep the
default alarm tone. Each app can have its own custom sound in a future update.

Volume — set the alarm volume independently from your phone's system volume using
the in-app slider.

Vibration — enable or disable the vibration pattern that plays alongside the alarm.

Wake Screen — control whether the app turns on your screen when an alarm fires.

Repeat Alarm — set how many times the alarm repeats and at what interval, so a missed
alarm keeps trying to get your attention.

Do Not Disturb Schedule — set a quiet window (for example 11 PM to 7 AM) during which
no alarms will fire, so your sleep is protected.

Flash LED — optionally flash your phone's camera LED when an alarm triggers.

Per-App Control — individually enable or disable alarm monitoring for each installed
app on your phone. Upwork and Fiverr are pre-enabled by default.


📋 ALARM HISTORY

Every alarm that fires is saved to a searchable history log showing the app name,
notification title, full content, exact timestamp, and special badges for Upwork Best
Matches and Fiverr notifications. You can clear the entire history with one tap whenever
you want.


🌙 DARK MODE & MATERIAL DESIGN 3

Message Alarm is built with Google's latest Material Design 3 guidelines and fully
supports both light and dark mode, automatically following your system preference. Every
screen — the home dashboard, the apps list, the alarm popup, the history log, and the
settings — is designed to look clean, modern, and easy to use with one hand.


🛡️ PRIVACY & SECURITY

Your privacy is taken seriously. Message Alarm does not collect any personal data, does
not send your notifications to any server, does not require an account or login, and
does not connect to any third-party analytics service. All notification data stays
entirely on your device and is stored locally in an encrypted Room database. The app
only reads notifications from apps you explicitly enable — nothing else.

The APK is signed with a release keystore and built with R8 minification and ProGuard
obfuscation, making it fully compliant with Google Play Protect. No warnings, no
unknown developer alerts, no security flags.


🚀 BUILT WITH MODERN ANDROID ARCHITECTURE

Message Alarm is engineered to the highest Android development standards:

- Language: Kotlin — 100% Kotlin, zero Java
- Architecture: MVVM (Model-View-ViewModel) with clean separation of concerns
- Database: Room — local SQLite database with type-safe DAOs and Flow support
- Preferences: DataStore — modern replacement for SharedPreferences
- Async: Kotlin Coroutines and Flow — reactive, non-blocking background processing
- UI: Material Design 3 with ViewBinding — no findViewById, type-safe view access
- Navigation: AndroidX Navigation Component with bottom navigation
- Background: NotificationListenerService + ForegroundService for reliable detection
- Build: Gradle with version catalog (libs.versions.toml) for clean dependency management
- CI/CD: GitHub Actions for fully automated APK building and signing


🤖 AUTOMATED APK BUILDS VIA GITHUB ACTIONS

The entire APK build and signing process is automated through GitHub Actions. Every time
code is pushed to the main branch, GitHub automatically sets up the build environment,
generates a signed release keystore, compiles the Kotlin source code, runs lint checks,
builds the release APK with full R8 optimisation, verifies the APK signature, and
uploads the final signed APK as a downloadable artifact. No Android Studio required on
your machine. Just push your code and download your APK.

When you push a version tag like v1.0.0, a full GitHub Release is created automatically
with the APK attached for direct download and distribution.


👨‍💻 DEVELOPED BY ADESH CHANDRA

Message Alarm is developed and maintained by Adesh Chandra, an Android developer
specialising in Kotlin, Material Design, and production-quality Android applications.

Need a custom Android app built for your business or project? Reach out on Fiverr at
fiverr.com/adesh_chandra
