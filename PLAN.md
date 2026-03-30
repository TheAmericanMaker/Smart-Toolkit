# Smart Utilities — Feature Upgrade Plan

The notepad is now the gold standard: rich editing, persistence, undo, voice input, templates, export/import, smart categorization, swipe actions, subcategories with indent, changeable icons, auto-capitalize, and auto-collapsing header. The goal is to bring the other 21 utilities up in quality using the same patterns.

---

## Phase 1: Cross-Cutting Improvements — ✅ ~95% COMPLETE

1. **Session persistence for stateful tools** — ✅ COMPLETE
   - ✅ Tally Counter — persists via DataStore
   - ✅ Timer — persists config via DataStore
   - ✅ Stopwatch — persists accumulated time and laps via DataStore

2. **Haptic feedback** — ✅ COMPLETE
   - ✅ Calculator, Timer, Stopwatch, Random Generator, Tally Counter all have haptic feedback

3. **History / recent results**
   - ✅ Calculator — Room-backed, scrollable, clickable to reuse
   - ✅ Random Generator — Room-backed persistent history
   - ✅ Unit Converter — Room-backed history with toggle panel
   - ❌ Tip Calculator — no history (low priority, tips are ephemeral)

---

## Phase 2: Sensor & Hardware Tools — ~85% COMPLETE

4. **Stopwatch** — ✅ COMPLETE
   - ✅ Lap deletion (swipe-to-dismiss)
   - ✅ Export laps to clipboard
   - ✅ Background persistence via foreground service notification

5. **Timer** — ✅ COMPLETE
   - ✅ Quick-select presets (1m, 3m, 5m, 10m, 15m, 30m)
   - ✅ Persistent countdown notification via foreground service
   - ✅ Repeat timer option (toggle in config, auto-restarts with brief alarm)

6. **Compass** — ⚠️ PARTIAL
   - ✅ Accuracy indicator (color-coded HIGH/MEDIUM/LOW/UNRELIABLE)
   - ✅ Lock bearing feature
   - ❌ Magnetic vs true north toggle with declination correction

7. **Sound Meter** — ⚠️ PARTIAL
   - ✅ Min/max tracking with display
   - ✅ Scrolling history chart (Canvas-based waveform, last 100 readings)
   - ✅ Average (avg) tracking with real-time display and CSV export
   - ❌ Calibration offset setting

8. **Bubble Level** — ✅ COMPLETE
   - ✅ Haptic feedback when surface is level
   - ✅ Calibration UX with "Zero on flat surface" button

---

## Phase 3: Camera-Based Tools — ~50% COMPLETE

9. **QR Scanner** — ⚠️ PARTIAL
   - ✅ Scan history (Room-backed, delete, clear all)
   - ❌ Generate QR code feature

10. **Color Picker** — ⚠️ PARTIAL
    - ✅ Palette history (Room-backed, horizontal scroll)
    - ❌ Pick from gallery image
    - ❌ Color name lookup
    - ❌ Export palette

11. **Magnifying Glass** — ⚠️ PARTIAL
    - ✅ Preset zoom levels (2x, 4x, 6x, 8x chips)
    - ✅ Torch toggle
    - ❌ Freeze/capture button to snapshot current view

---

## Phase 4: Computational Tools — ~95% COMPLETE

12. **Calculator** — ✅ COMPLETE
    - ✅ Scrollable expression history (Room-persisted)
    - ✅ Copy result button
    - ✅ Percentage button
    - ✅ Scientific calculator toggle

13. **Unit Converter** — ✅ MOSTLY COMPLETE
    - ✅ Persist last-used category and units (DataStore)
    - ✅ Swap from/to units
    - ✅ Conversion history (Room-backed, toggle panel in top bar)
    - ❌ Favorites/pinned conversions (low priority)

14. **Text Tools** — ✅ COMPLETE
    - ✅ Find & replace with match count
    - ✅ Word frequency analysis (top 30)
    - ✅ Remove duplicate lines
    - ✅ Sort lines
    - ✅ Bonus: uppercase, lowercase, title case, reverse, trim

15. **Random Generator** — ✅ COMPLETE
    - ✅ Generation history (Room-backed, persistent)
    - ✅ Batch generate (configurable count 1-100)
    - ✅ List shuffler mode (SHUFFLE tab, comma/newline input)

16. **Tip Calculator** — ✅ COMPLETE
    - ✅ Rounding options (none, round total, round per person)
    - ✅ Tax input field
    - ✅ Persist last tip percentage preference

---

## Phase 5: Info & Simple Tools — ~75% COMPLETE

17. **Battery** — ⚠️ PARTIAL
    - ✅ Real-time status, temperature (C/F), voltage, health
    - ❌ Charge session logging (track charge/discharge over time)
    - ❌ Estimated time to full/empty

18. **Network** — ⚠️ PARTIAL
    - ✅ Ping/latency test (8.8.8.8)
    - ✅ Connection type, IP address, WiFi signal strength
    - ❌ Connection history log

19. **Device Info** — ✅ COMPLETE
    - ✅ Grouped by category (Device, Software, Hardware, Display)
    - ✅ Copy all info to clipboard
    - ✅ Comprehensive details (model, manufacturer, Android version, API, RAM, CPU, screen)

20. **Storage** — ✅ COMPLETE
    - ✅ Per-category breakdown (Internal vs External)
    - ✅ Donut chart visualization (Canvas-based arc)
    - ✅ Used, available, total display

21. **Flashlight** — ✅ COMPLETE
    - ✅ SOS mode (Morse code pattern)
    - ✅ Strobe mode with adjustable speed (50-500ms slider)
    - ✅ Foreground service for persistent control

22. **Ruler** — ✅ COMPLETE
    - ✅ Calibration flow with DPI offset
    - ✅ Calibration persistence (DataStore)
    - ✅ Metric/Imperial toggle

23. **Tally Counter** — ⚠️ PARTIAL
    - ✅ Persist count across sessions (DataStore)
    - ✅ Reset confirmation dialog
    - ✅ Foreground service notification
    - ❌ Named counters (multiple tallies)

---

## Remaining Work (all ❌ items above)

### Medium effort:
- Compass true north toggle with declination
- Sound Meter calibration offset
- Magnifying Glass freeze/capture
- Color Picker: pick from gallery, color name lookup
- Network connection history log
- Tally Counter named counters
- Device Info search/filter

### Larger features:
- QR Code generation
- Color Picker palette export
- Battery charge session logging + time estimates

---

## Implementation Patterns (from notepad)
- Use `SwipeToDismissBox` for deletable list items (history, saved items)
- Use `LaunchedEffect` + `SnackbarHost` for undo flows
- Use `DataStore` for preferences, `Room` for structured data
- Use `UtilityTopBar` actions for share/export
- Keep voice input where text entry exists
- Use `rememberHaptic()` helper for haptic feedback
