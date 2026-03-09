# 🔔 Message Alarm

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Play%20Protect-Safe-brightgreen?style=for-the-badge&logo=google-play" />
</p>

<p align="center">
  A powerful notification alarm app for Android — built specifically for <strong>Fiverr</strong> and <strong>Upwork</strong> freelancers.<br/>
  Never miss a new message, order, or job match again.
</p>

<p align="center">
  <strong>Developed by <a href="https://fiverr.com/adesh_chandra">Adesh Chandra</a></strong>
</p>

---

## 📱 Screenshots

> App screens: Home Dashboard · Apps List · Alarm Popup · Settings

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔔 **Notification Alarm** | Triggers a loud alarm for any enabled app |
| ⭐ **Upwork Best Matches** | Special alarm only when Best Match jobs appear |
| 🟢 **Fiverr Alerts** | Alarm for new messages, orders, and buyer requests |
| 📱 **Full-Screen Popup** | Shows app name, title, and message content |
| ⏸️ **Snooze** | Snooze the alarm for 5 / 10 / 15 / 30 minutes |
| 🛑 **Stop Alarm** | Stop instantly from the popup or notification bar |
| 🎵 **Custom Sound** | Pick any ringtone as your alarm sound |
| 📳 **Vibration** | Vibrate along with the alarm |
| 💡 **Wake Screen** | Turns on screen when alarm triggers |
| 🔁 **Repeat Alarm** | Repeats alarm up to N times at set intervals |
| 🌙 **Do Not Disturb** | Schedule quiet hours (e.g. 10 PM – 7 AM) |
| 📋 **History** | Full log of every alarm that was triggered |
| 🌓 **Dark Mode** | Full Material 3 dark mode support |
| 🛡️ **Play Protect Safe** | Properly signed — no warnings on install |

---

## 🚀 How to Build APK via GitHub Actions

> No Android Studio needed. GitHub builds and signs everything automatically.

### Step 1 — Add 1 Secret to GitHub

Go to your repository on GitHub:

```
Settings → Secrets and variables → Actions → New repository secret
```

Add this one secret:

| Secret Name | Value |
|---|---|
| `KEYSTORE_PASSWORD` | Any strong password you choose, e.g. `MyAlarm2024!` |

### Step 2 — Push Code to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/MessageAlarm.git
git push -u origin main
```

### Step 3 — Download Your APK

1. Go to your repo on GitHub
2. Click the **Actions** tab
3. Click the latest workflow run
4. Scroll down to **Artifacts**
5. Download **MessageAlarm-Release-APK** ✅

### Step 4 — Create a Release (Optional)

Push a version tag to automatically create a GitHub Release with the APK attached:

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## 📲 How to Install on Your Phone

1. Download the APK to your Android phone
2. Go to **Settings → Security → Install unknown apps** → Allow
3. Open the APK file and tap **Install**
4. Open **Message Alarm**
5. Tap **Grant Permission** and enable Notification Access for the app
6. Go to **Apps tab** → toggle on **Upwork** and **Fiverr**
7. Done! 🎉

---

## 🏗️ Project Structure

```
MessageAlarm/
├── .github/
│   └── workflows/
│       └── build-release.yml       ← GitHub Actions (auto-build & sign)
├── app/
│   ├── build.gradle.kts            ← App dependencies & signing config
│   ├── proguard-rules.pro          ← R8/ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/adeshchandra/messagealarm/
│       │   ├── MainActivity.kt         ← Entry point, bottom navigation
│       │   ├── AlarmActivity.kt        ← Full-screen alarm popup
│       │   ├── MessageAlarmApp.kt      ← Application class
│       │   ├── alarm/
│       │   │   ├── AlarmService.kt     ← Plays sound + vibration
│       │   │   └── AlarmReceivers.kt   ← Stop & Snooze broadcast receivers
│       │   ├── service/
│       │   │   ├── NotificationAlarmService.kt  ← Core: reads notifications
│       │   │   └── BootReceiver.kt              ← Restart on boot
│       │   ├── data/
│       │   │   ├── db/             ← Room database + DAOs
│       │   │   ├── model/          ← AppConfig, NotificationEvent
│       │   │   └── prefs/          ← DataStore preferences
│       │   └── ui/
│       │       ├── home/           ← Dashboard fragment
│       │       ├── apps/           ← App list with toggles
│       │       ├── history/        ← Alarm history
│       │       └── settings/       ← Settings screen
│       └── res/
│           ├── layout/             ← All XML layouts
│           ├── drawable/           ← Icons and graphics
│           ├── navigation/         ← Nav graph
│           └── values/             ← Colors, strings, themes
├── gradle/
│   ├── libs.versions.toml          ← Dependency versions
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
└── README.md
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **MVVM Architecture** | Clean separation of UI and logic |
| **Room Database** | Local storage for notification history |
| **DataStore** | Storing app settings/preferences |
| **Kotlin Coroutines + Flow** | Async operations and reactive UI |
| **Material Design 3** | Modern UI components |
| **AndroidX Navigation** | Fragment navigation with bottom nav |
| **NotificationListenerService** | Core: reads incoming notifications |
| **R8 / ProGuard** | Code shrinking and obfuscation |

---

## 🔒 Permissions Explained

| Permission | Why it's needed |
|---|---|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read incoming notifications to trigger alarms |
| `FOREGROUND_SERVICE` | Keep the service running in the background |
| `VIBRATE` | Vibrate when alarm triggers |
| `WAKE_LOCK` | Wake the screen when alarm triggers |
| `RECEIVE_BOOT_COMPLETED` | Restart service after phone reboot |
| `POST_NOTIFICATIONS` | Show the persistent service notification |
| `SCHEDULE_EXACT_ALARM` | Snooze alarm at precise time |
| `INTERNET` | Optional: future update notifications |

> ✅ No SMS, no Contacts, no Location, no Camera — only what's truly needed.

---

## 🛡️ Play Protect Compliance

This app is designed to pass Google Play Protect checks:

- ✅ Release-signed APK (never debug keystore)
- ✅ R8/ProGuard minification and obfuscation enabled
- ✅ Only necessary permissions declared
- ✅ No `BIND_ACCESSIBILITY_SERVICE`
- ✅ No `INSTALL_PACKAGES`
- ✅ No runtime code execution or dynamic loading
- ✅ Target SDK 35 (latest Android)
- ✅ HTTPS only for all network calls

---

## 🔧 GitHub Actions Workflow Overview

The workflow at `.github/workflows/build-release.yml` does the following automatically on every push:

1. **Checks out** your code
2. **Sets up** Java 17 (Temurin)
3. **Downloads** the Gradle wrapper JAR
4. **Caches** Gradle dependencies (faster builds)
5. **Auto-generates** a release keystore using `keytool` + your `KEYSTORE_PASSWORD` secret
6. **Runs lint** check on the code
7. **Builds** the release APK with R8 minification
8. **Verifies** the APK signature with `apksigner`
9. **Deletes** the keystore (security)
10. **Renames** APK to `MessageAlarm-vX.X.X-release.apk`
11. **Uploads** APK as a downloadable artifact (kept 30 days)
12. **Creates a GitHub Release** automatically when you push a `v*` tag

---

## 📦 Build Variants

| Variant | Signed | Minified | Use for |
|---|---|---|---|
| `debug` | Debug key | No | Development & testing |
| `release` | Release key | Yes (R8) | Distribution & production |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

```
Copyright 2024 Adesh Chandra

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 👨‍💻 Developer

**Adesh Chandra**

- 🌐 Fiverr: [fiverr.com/adesh_chandra](https://fiverr.com/adesh_chandra)
- 💼 Android Developer | Kotlin | Material Design

> Need a custom Android app? Feel free to reach out on Fiverr!

---

<p align="center">Made with ❤️ for freelancers on Fiverr & Upwork</p>
