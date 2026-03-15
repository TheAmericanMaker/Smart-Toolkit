**A collection of 21 handy everyday tools in one Android app.** Built with Jetpack Compose and Material 3.

Provided by **The American Maker & Claude Code**

---

## 🧰 21 Utilities

### Time & Productivity
- **Stopwatch** — Precision timing with lap tracking, swipe-to-delete laps, and notification tray controls (pause/resume/lap/stop)
- **Timer** — Countdown with quick presets (1m–30m), background service, notification controls (pause/resume/cancel/dismiss), and selectable alarm sounds
- **Calculator** — Expression-based with history panel — tap any result to reuse it
- **Notepad** — Full note-taking with Room database, voice dictation at cursor position, and color labels
- **Tally Counter** — Persistent counter with pin-to-notification (+1/−1 buttons right in the tray)

### Measurement & Sensors
- **Compass** — Digital compass with accuracy indicator, lock bearing marker, and cardinal labels
- **Bubble Level** — Surface and side level with calibration and haptic feedback when level
- **Sound Meter** — Ambient dB measurement with color-coded gauge, live scrolling chart, and min/max tracking
- **Ruler** — On-screen ruler (cm/mm and inches) with DPI calibration slider

### Device & Network
- **Battery** — Real-time stats with color-coded arc gauge, tap to toggle °C/°F
- **Network** — Connection info with visual signal bars and ping test (latency to 8.8.8.8)
- **Storage** — Internal/external usage with arc chart and detailed breakdown
- **Device Info** — Model, CPU, RAM, and screen specs in organized sections

### Camera & Visual
- **Flashlight** — Steady, SOS (morse code), and Strobe modes with notification tray Turn Off button
- **QR Scanner** — Scan QR/barcodes with history, swipe-to-delete, tap to re-view
- **Magnifier** — Camera-based zoom with presets (2×–8×), slider, and torch toggle
- **Color Picker** — Live camera color sampling with HEX/RGB/HSL, save palette (last 20), tap to copy

### Converters & Generators
- **Unit Converter** — 7 categories with persisted selections, copy result, and conversion formula display
- **Text Tools** — Character/word counts, case transforms, find & replace, remove duplicates, sort lines, word frequency
- **Random Generator** — Numbers, dice, coin, and passwords with batch generation (1–100) and history
- **Tip Calculator** — Bill splitting with tax field, custom tip percentages, and rounding modes

---

## 📱 Notification Tray Controls

Interact with running utilities without opening the app:

| Utility | Notification Actions |
|---------|---------------------|
| Timer | Pause / Resume / Cancel / Dismiss alarm |
| Stopwatch | Pause / Resume / Lap / Stop |
| Flashlight | Turn Off |
| Tally Counter | +1 / −1 |

Tap any notification to jump back to that utility's screen.

---

## 🎨 Customization

- **8 color themes** — Dynamic (Material You), Blue, Red, Green, Purple, Orange, Pink, Gold
- **Dark mode** — Follow system, or manually toggle dark/light
- **Favorites** — Star utilities for quick access on the home dashboard
- **Search** — Find any utility instantly from the home screen

---

## 📖 In-App User Guide

Searchable help available at **Settings > Help > User Guide**. Covers every utility with how-to instructions, tips, and permission explanations. Sections are collapsible and filterable in real time.

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM with StateFlow |
| DI | Hilt |
| Navigation | Navigation Compose |
| Storage | Room (notes, history) + DataStore Preferences |
| Camera | CameraX + ML Kit Barcode Scanning |
| Background | Foreground services with notification controls |
| Sensors | Accelerometer, magnetometer, microphone |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

---

## Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/smart-toolkit-debug.apk`
