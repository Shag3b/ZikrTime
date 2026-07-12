<div align="center">

# 🌙 ZikrTime

**Your daily companion for Morning & Evening Azkar**

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-blue)](https://developer.android.com/tools/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

*A clean, offline-first Android app that reminds and guides Muslims through their daily Morning and Evening Azkar — on time, every day.*

</div>

---

## 📸 Overview

ZikrTime is a lightweight, privacy-first Android application built with **Jetpack Compose** that helps Muslims maintain their daily Azkar habit. The app uses real prayer times calculated from the device's GPS location to determine the exact window for Morning (Fajr → Asr) and Evening (Asr → Midnight) Azkar — no internet required after first location fetch.

---

## 📥 Download

You can download and install the latest compiled production-ready APK directly from:
- [📦 **Download APK (MediaFire)**](https://www.mediafire.com/folder/q6ra32g3qway3/Athkar)
- *Google Play Store link is coming soon! (Currently unpublished / under review)*

---

## ✨ Features

| Feature | Description |
|---|---|
| 🌅 **Morning Azkar** | Complete morning remembrances during the Fajr → Asr window |
| 🌆 **Evening Azkar** | Complete evening remembrances during the Asr → Midnight window |
| 🔔 **Smart Notifications** | Reminders every 15 minutes if Azkar are not completed during the active window |
| 📍 **Prayer Time Calculation** | Uses GPS location + Adhan library for precise local prayer times |
| 📊 **Daily Stats & Streaks** | Track completion streaks, total completions, and days forgotten |
| 🌐 **Bilingual (AR / EN)** | Full Arabic and English support with RTL layout |
| 📴 **Offline First** | All Azkar data stored locally as JSON — no internet needed |
| ♻️ **Auto Reset** | State resets automatically at Fajr and Asr via AlarmManager |
| 🔄 **Boot & Update Aware** | Alarms reschedule automatically after device reboot or app update |
| 🛡️ **WorkManager Fallback** | Background safety net if AlarmManager fails |

---

## 🏗️ Architecture & Project Structure

```
ZikrTime/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── morning.json          # Morning Azkar data (Arabic, Franco, meaning)
│   │   │   └── evening.json          # Evening Azkar data
│   │   ├── java/com/shaghb/zikrtime/
│   │   │   ├── data/
│   │   │   │   ├── Zikr.kt           # Data model
│   │   │   │   └── ZikrRepository.kt # Loads Azkar from JSON assets
│   │   │   ├── notification/
│   │   │   │   ├── ZikrNotificationHelper.kt       # Notification channels & display
│   │   │   │   └── NotificationPermissionManager.kt # Android 13+ permission flow
│   │   │   ├── ui/
│   │   │   │   ├── permission/
│   │   │   │   │   └── PermissionDialogs.kt  # Permission rationale screens
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt          # Main menu & statistics
│   │   │   │   │   ├── ZikrScreen.kt          # Active Azkar counter interface
│   │   │   │   │   ├── DoneScreen.kt          # Completion screen
│   │   │   │   │   └── LanguageSelectionScreen.kt # Language onboarding screen
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   ├── utils/
│   │   │   │   ├── LocationManager.kt    # GPS location fetch
│   │   │   │   └── PrayerTimeHelper.kt  # Adhan prayer time wrapper
│   │   │   ├── worker/
│   │   │   │   ├── NotificationFallbackWorker.kt  # WorkManager fallback
│   │   │   │   └── WorkManagerScheduler.kt
│   │   │   ├── MainActivity.kt           # Entry point + nav host
│   │   │   ├── ReminderScheduler.kt      # AlarmManager scheduling
│   │   │   ├── BootReceiver.kt           # Reschedules on device boot
│   │   │   ├── AppInstallReceiver.kt     # Reschedules on app update
│   │   │   ├── ReminderReceiver.kt       # Fires reminder notifications
│   │   │   ├── ResetReceiver.kt          # Resets daily state at prayer times
│   │   │   ├── InitialNotificationReceiver.kt  # First notification of the period
│   │   │   ├── UnlockReceiver.kt         # Triggers on screen unlock
│   │   │   ├── DailyStateManager.kt      # Tracks morning/evening completion
│   │   │   ├── AzkarProgressManager.kt   # Saves mid-session progress
│   │   │   ├── StatsManager.kt           # Streak & completion statistics
│   │   │   ├── TimeChecker.kt            # Determines active Azkar period
│   │   │   ├── FirstLaunchManager.kt     # Handles first-run onboarding
│   │   │   └── LanguageManager.kt        # Language preference management
│   │   ├── res/                          # Strings, drawables, XML configs
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml              # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin (100%) |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Navigation** | Jetpack Compose Navigation |
| **Concurrency** | Kotlin Coroutines |
| **Background Work** | WorkManager (safety net check) |
| **Scheduling** | AlarmManager (exact alarms) |
| **JSON Parser** | Gson |
| **Prayer Times** | Adhan Java Library (by Batoul Apps) |

---

## 💡 Key Implementation Details

### Background Reliability vs OS Restrictions
Modern Android versions enforce strict background execution limits (Doze mode, standby buckets). ZikrTime implements a hybrid scheduling pattern to ensure notifications fire reliably without draining the battery:
1. **AlarmManager (`setExactAndAllowWhileIdle`)**: Schedules the exact daily reset times (at Fajr and Asr) and periodic 15-minute reminders during active periods.
2. **WorkManager Fallback**: Schedules background fallback checks every 15 minutes as a safety net. If Android kills the `AlarmManager` queue, the fallback worker detects it and reschedules the alarms.

### Offline-First Architecture
Azkar text (Arabic, Transliterated/Franco, and English translations) are stored locally in the assets directory. They are parsed asynchronously when a session starts. All state resets, completions, and streaks are tracked locally via `SharedPreferences`.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 26 (Android 8.0 Oreo) or higher
- Gradle 8.0+

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/shaghb/ZikrTime.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the application on your device or emulator.

---

## 🤝 Contributing

Contributions are welcome! If you'd like to improve ZikrTime:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📬 Contact

**Shaghb**
[![GitHub](https://img.shields.io/badge/GitHub-shaghb-181717?logo=github)](https://github.com/shaghb)

---

## 📝 License

```
MIT License

Copyright (c) 2026 Shaghb

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">
Made with ❤️ for the Muslim community &nbsp;|&nbsp; بارك الله فيكم
</div>
