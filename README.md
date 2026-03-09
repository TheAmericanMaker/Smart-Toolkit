# Smart Toolkit

A collection of 21 handy everyday tools in one Android app. Built with Jetpack Compose and Material 3.

Provided by **Mid Michigan MFG, LLC** — [midmichiganmfg.com](https://midmichiganmfg.com)

## Features

| Utility | Description |
|---------|-------------|
| Flashlight | Toggle device flashlight on/off |
| Stopwatch | Precision stopwatch with lap tracking |
| Timer | Countdown timer with alarm and vibration |
| Calculator | Basic and scientific calculator with expression parsing |
| Battery | Real-time battery status, temperature, voltage, and health |
| Compass | Digital compass with animated compass rose |
| Network | Connection type, IP address, WiFi signal info |
| Storage | Internal/external storage usage analyzer |
| Ruler | On-screen ruler with labeled cm/mm and inch markings |
| QR Scanner | Scan QR codes and barcodes using CameraX + ML Kit |
| Unit Converter | Convert between units across 7 categories |
| Text Tools | Character/word/sentence counts, case transforms, clipboard |
| Random Generator | Random numbers, dice, coin flip, password generator |
| Notepad | Simple note-taking with local Room database |
| Device Info | Model, CPU, RAM, screen specs, Android version |
| Sound Meter | Approximate ambient sound level measurement |
| Bubble Level | Surface and side level with user calibration zeroing |
| Tip Calculator | Bill splitting with customizable tip percentages |
| Tally Counter | Persistent tap counter with haptic feedback |
| Magnifier | Camera-based magnifying glass with zoom and torch |
| Color Picker | Live camera color sampling with HEX, RGB, and HSL output |

## Screenshots

*Coming soon*

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 with dynamic color
- **Architecture:** MVVM with StateFlow
- **DI:** Hilt
- **Navigation:** Navigation Compose
- **Local Storage:** Room (notes), DataStore Preferences (settings/favorites), SharedPreferences (tally counter)
- **Camera:** CameraX + ML Kit Barcode Scanning
- **Sensors:** Accelerometer (compass, bubble level), microphone (sound meter)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

## Building

### Prerequisites

- Android Studio Hedgehog or newer
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

This project uses GitHub Actions to automatically build a debug APK on every push to `main` and on pull requests. Download the APK from the workflow artifacts.

## Project Structure

```
app/src/main/java/com/smartutilities/app/
├── MainActivity.kt              # Single activity entry point
├── SmartToolkitApp.kt           # Hilt application class
├── navigation/                  # Route definitions and NavHost
├── ui/
│   ├── theme/                   # Material 3 theme, colors, typography
│   ├── home/                    # Dashboard with search and favorites
│   ├── settings/                # Dark mode and app settings
│   └── components/              # Shared UI components
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
├── data/                        # Room database, DataStore, models
└── di/                          # Hilt modules
```

## License

See [LICENSE](LICENSE) for details.
