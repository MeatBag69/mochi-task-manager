# 🐰🌸 Mochi Task Manager

> **A cute but powerful Android system monitor and task manager with animated mascot reactions and widgets.**

Designed specifically for modern Android devices (including Samsung Galaxy S21 FE / One UI), **Mochi Task Manager** gives you granular, real-time insights into your phone's hardware, CPU, RAM, battery, thermals, and active background processes — with an adorable interactive bunny assistant named **Mochi** who reacts dynamically to your phone's state!

---

## ✨ Features

- 🐰 **Interactive Mochi Mascot:**
  - Dynamic real-time reactions based on phone state (charging, battery stages, overheating, high RAM, healthy).
  - Tap interaction with multiple animated expressions and reaction bubbles.
  - Special **Battery Protection Guard Mode** (Knight Mochi with shield & spear when charging is capped at 80–85%).
- 📊 **Live Performance & Process Monitor:**
  - Real hardware memory read directly from `/proc/meminfo`.
  - True CPU usage, core speeds, and system load.
  - Active foreground/background process inspection using Android `UsageEvents` and `UsageStats`.
- 🔋 **Battery Intelligence & Thermal Guardian:**
  - Dynamic battery health metrics, real-time temperature in °C, charging state, and drain tracking.
- 💾 **Storage & Clean Analyzer:**
  - Real-time partition usage and breakdown.
- 📱 **Custom Home Screen Widgets:**
  - 2×2 Quick Status, 4×2 Performance Graphs, and 4×3 Detailed Monitors with live-updating mascot poses.
- 🎨 **Theme System:**
  - System default auto-detection (Light / Dark) on first launch, with in-app manual theme customization.

---

## 🛠️ Tech Stack & Architecture

- **Platform:** Android 8.0+ (API 26) up to Android 16 (API 36)
- **Language:** Kotlin 2.1.0
- **Build System:** Gradle 8.12 + Android Gradle Plugin 8.6.0
- **Architecture:** Zero-bloat native Android framework APIs with high-performance `RemoteViews` for widgets.

---

## 🚀 Building & Installing

### Prerequisites
- Java JDK 17 or higher
- Android SDK (API 35/36)

### Build Debug APK
```bash
./gradlew assembleDebug --offline
```

### Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell cmd appops set --user 0 com.cuteterminal.widget GET_USAGE_STATS allow
```

---

## 📄 License
MIT License
