# Smart Toolkit

![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue.svg)

A collection of 21 handy everyday tools in one Android app. Built with Jetpack Compose and Material 3.

Provided by **The American Maker & Claude Code**

## Features

| Utility | Description |
|---------|-------------|
| Flashlight | Toggle flashlight with Steady, SOS (morse code), and Strobe modes; notification tray control with Turn Off button |
| Stopwatch | Precision stopwatch with lap tracking, swipe-to-delete laps, haptic feedback; notification tray controls (pause/resume/lap/stop) |
| Timer | Countdown timer with quick presets (1m–30m), background foreground service, notification controls (pause/resume/cancel/dismiss from tray), selectable alarm sounds |
| Calculator | Expression-based calculator with history panel, tap to reuse results, haptic feedback |
| Battery | Real-time battery stats with color-coded arc gauge, tap to toggle °C/°F |
| Compass | Digital compass with accuracy indicator, lock bearing marker, cardinal labels |
| Network | Connection info with visual signal bars, ping test (latency to 8.8.8.8) |
| Storage | Internal/external usage with arc chart, detailed breakdown, refresh |
| Ruler | On-screen ruler (cm/mm and inches) with DPI calibration slider |
| QR Scanner | Scan QR/barcodes with scan history, swipe-to-delete, tap to re-view |
| Unit Converter | Convert across 7 categories, persisted selections, copy result, conversion formula |
| Text Tools | Counts, case transforms, find & replace, remove duplicate lines, sort lines, word frequency |
| Random Generator | Numbers, dice, coin, passwords with batch generation (1–100) and history |
| Notepad | Rich note-taking with Room database, voice dictation at cursor, color labels |
| Device Info | Model, CPU, RAM, screen specs grouped into Device/Software/Hardware/Display sections |
| Sound Meter | Ambient dB measurement with color-coded gauge, live scrolling chart, min/max tracking |
| Bubble Level | Surface and side level with calibration, haptic feedback on level |
| Tip Calculator | Bill splitting with tax field, custom tips, rounding modes (total/per person) |
| Tally Counter | Persistent counter (DataStore), reset confirmation dialog, haptic feedback; pin-to-notification with +1/−1 buttons |
| Magnifier | Camera-based magnifier with zoom presets (2x–8x), slider, and torch |
| Color Picker | Live camera color sampling with HEX/RGB/HSL, save palette (last 20), tap to copy |

## User Guide

A searchable in-app User Guide is available under **Settings > Help > User Guide**. It covers every utility with how-to instructions, tips, and permission explanations. Sections are collapsible and entries expand on tap. The search bar filters by title, content, and tags in real time.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 with dynamic color
- **Architecture:** MVVM with StateFlow
- **DI:** Hilt
- **Navigation:** Navigation Compose
- **Local Storage:** Room (notes, history), DataStore Preferences (settings, favorites, utility state)
- **Camera:** CameraX + ML Kit Barcode Scanning
- **Background:** Foreground services (timer, stopwatch, flashlight, tally counter notification controls)
- **Sensors:** Accelerometer (compass, bubble level), magnetometer (compass), microphone (sound meter)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 35 (Android 15)

## Building

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17

### Build Debug APK

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/smart-toolkit-debug.apk`.

### Build Release APK

```bash
./gradlew assembleRelease
```

> Note: Release builds require signing configuration. See [Android signing docs](https://developer.android.com/studio/publish/app-signing).

## CI/CD

This project uses GitHub Actions to build a debug APK on demand. Go to the **Actions** tab and run the "Build APK" workflow manually. Download the APK from the workflow artifacts.

## Project Structure

```
app/src/main/java/com/smarttoolkit/app/
├── MainActivity.kt              # Single activity entry point
├── SmartToolkitApp.kt           # Hilt application class
├── navigation/                  # Route definitions and NavHost
├── ui/
│   ├── theme/                   # Material 3 theme, colors, typography
│   ├── home/                    # Dashboard with search and favorites
│   ├── settings/                # Dark mode and app settings
│   ├── guide/                   # Searchable in-app User Guide
│   ├── components/              # Shared UI components (PermissionHandler, UtilityTopBar)
│   └── util/                    # Haptic feedback utility
├── feature/                     # One package per utility
│   ├── flashlight/
│   ├── stopwatch/
│   ├── timer/
│   ├── calculator/
│   ├── battery/
│   ├── compass/
│   ├── network/
│   ├── storage/
│   ├── ruler/
│   ├── qrscanner/
│   ├── unitconverter/
│   ├── texttools/
│   ├── randomgenerator/
│   ├── notepad/
│   ├── deviceinfo/
│   ├── soundmeter/
│   ├── bubblelevel/
│   ├── tipcalculator/
│   ├── tallycounter/
│   ├── magnifyingglass/
│   └── colorpicker/
├── data/
│   ├── billing/                 # In-app purchase (remove ads)
│   ├── db/                      # Room database, HistoryEntry/DAO, migrations
│   ├── model/                   # Data models (Note, UtilityItem)
│   ├── preferences/             # DataStore preferences repository
│   └── repository/              # Note repository
└── di/                          # Hilt modules (database, DataStore)
```

## License

See [LICENSE](LICENSE) for details.
