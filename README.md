# Smart Toolkit

[![Android CI](https://github.com/TheAmericanMaker/Smart-Toolkit/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/TheAmericanMaker/Smart-Toolkit/actions/workflows/build.yml)

Smart Toolkit is a local-first Android utility app with 21 everyday tools in a single Jetpack Compose app.

Provided by **The American Maker & Claude Code**

## Highlights

- 21 built-in tools covering timing, measurement, device info, note-taking, text manipulation, and camera utilities
- Modern Android stack: Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, CameraX, and ML Kit
- Local-first data model: notes, favorites, history, and settings stay on-device
- Searchable in-app user guide under **Settings > Help > User Guide**
- Optional AdMob and Google Play Billing support for release builds, with safe defaults for public source builds

## Included utilities

- Time and productivity: Stopwatch, Timer, Calculator, Notepad, Tally Counter
- Measurement and sensors: Compass, Bubble Level, Sound Meter, Ruler
- Device and network: Battery, Network, Storage, Device Info
- Camera and visual tools: Flashlight, QR Scanner, Magnifier, Color Picker
- Converters and generators: Unit Converter, Text Tools, Random Generator, Tip Calculator

## Requirements

- Android Studio with Android SDK Platform 35 installed, or a command-line environment with `ANDROID_SDK_ROOT`
- JDK 17
- Android 8.0+ device or emulator for running the app (`minSdk = 26`)

## Quick start

1. Clone the repository and open it in Android Studio, or ensure `ANDROID_SDK_ROOT` points at a local Android SDK installation.
2. Build a debug APK:

```bash
./gradlew assembleDebug
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Release builds

Google Play releases should be produced as Android App Bundles:

```bash
./gradlew bundleRelease
```

The release bundle is written to `app/build/outputs/bundle/release/app-release.aab`.

Release builds require:

- a signing configuration for your release key
- optional AdMob and Google Play Billing configuration if you want ads and the "Remove Ads" purchase enabled

Release monetization is configured with Gradle properties or environment variables:

```properties
SMART_TOOLKIT_ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
SMART_TOOLKIT_ADMOB_BANNER_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
SMART_TOOLKIT_REMOVE_ADS_PRODUCT_ID=remove_ads
```

Recommended locations:

- `~/.gradle/gradle.properties` for local development
- repository or organization secrets in CI

Debug builds use Google demo ad units. Release builds disable ads and Google Play Billing unless the release properties above are provided.

For a maintainer-facing checklist, see [docs/RELEASING.md](docs/RELEASING.md).

## CI

GitHub Actions runs `testDebugUnitTest`, `assembleDebug`, and `bundleRelease` on pushes to `main`, pull requests, and manual workflow dispatches. The workflow uploads the debug APK and release bundle as artifacts for each successful run.

## Privacy

The current privacy policy lives at [PRIVACY_POLICY.md](PRIVACY_POLICY.md).

## Project layout

```text
app/src/main/java/com/smarttoolkit/app/
|-- MainActivity.kt            # Single activity entry point
|-- SmartToolkitApp.kt         # Hilt application class
|-- navigation/                # Routes and NavHost wiring
|-- ui/                        # Shared UI, theme, home, settings, user guide
|-- feature/                   # One package per utility
|-- data/                      # Room entities/DAOs and DataStore-backed preferences
`-- di/                        # Hilt modules
```

## License

No open source license has been assigned yet. Until a `LICENSE` file is added, the repository is not licensed for reuse or redistribution.
