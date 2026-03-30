# Smart Utilities — Feature Upgrade Plan

The notepad is now the gold standard: rich editing, persistence, undo, voice input, templates, export/import, smart categorization, swipe actions, subcategories with indent, changeable icons, auto-capitalize, and auto-collapsing header. The goal is to bring the other 21 utilities up in quality using the same patterns.

---

## Phase 1: Cross-Cutting Improvements — ✅ COMPLETE

1. **Session persistence for stateful tools** — ✅ COMPLETE
   - ✅ Tally Counter — persists via DataStore (JSON for named counters)
   - ✅ Timer — persists config via DataStore
   - ✅ Stopwatch — persists accumulated time and laps via DataStore

2. **Haptic feedback** — ✅ COMPLETE
   - ✅ Calculator, Timer, Stopwatch, Random Generator, Tally Counter all have haptic feedback

3. **History / recent results** — ✅ COMPLETE
   - ✅ Calculator — Room-backed, scrollable, clickable to reuse
   - ✅ Random Generator — Room-backed persistent history
   - ✅ Unit Converter — Room-backed history with toggle panel
   - ✅ Tip Calculator — Room-backed history with toggle panel

---

## Phase 2: Sensor & Hardware Tools — ✅ COMPLETE

4. **Stopwatch** — ✅ COMPLETE
   - ✅ Lap deletion (swipe-to-dismiss)
   - ✅ Export laps to clipboard
   - ✅ Background persistence via foreground service notification

5. **Timer** — ✅ COMPLETE
   - ✅ Quick-select presets (1m, 3m, 5m, 10m, 15m, 30m)
   - ✅ Persistent countdown notification via foreground service
   - ✅ Repeat timer option (toggle in config, auto-restarts with brief alarm)

6. **Compass** — ✅ COMPLETE
   - ✅ Accuracy indicator (color-coded HIGH/MEDIUM/LOW/UNRELIABLE)
   - ✅ Lock bearing feature
   - ✅ True north toggle with GeomagneticField declination correction

7. **Sound Meter** — ✅ COMPLETE
   - ✅ Min/max tracking with display
   - ✅ Scrolling history chart (Canvas-based waveform, last 100 readings)
   - ✅ Average (avg) tracking with real-time display and CSV export
   - ✅ Calibration offset setting (-20 to +20 dB slider, persisted via DataStore)

8. **Bubble Level** — ✅ COMPLETE
   - ✅ Haptic feedback when surface is level
   - ✅ Calibration UX with "Zero on flat surface" button

---

## Phase 3: Camera-Based Tools — ~75% COMPLETE

9. **QR Scanner** — ⚠️ PARTIAL
   - ✅ Scan history (Room-backed, delete, clear all)
   - ❌ Generate QR code feature

10. **Color Picker** — ✅ MOSTLY COMPLETE
    - ✅ Palette history (Room-backed, horizontal scroll)
    - ✅ Pick from gallery image (center pixel sampling via Photo Picker)
    - ✅ Color name lookup (55 CSS colors, nearest match by Euclidean distance)
    - ❌ Export palette

11. **Magnifying Glass** — ✅ COMPLETE
    - ✅ Preset zoom levels (2x, 4x, 6x, 8x chips)
    - ✅ Torch toggle
    - ✅ Freeze/capture button (captures PreviewView bitmap, toggle to unfreeze)

---

## Phase 4: Computational Tools — ✅ COMPLETE

12. **Calculator** — ✅ COMPLETE
    - ✅ Scrollable expression history (Room-persisted)
    - ✅ Copy result button
    - ✅ Percentage button
    - ✅ Scientific calculator toggle

13. **Unit Converter** — ✅ COMPLETE
    - ✅ Persist last-used category and units (DataStore)
    - ✅ Swap from/to units
    - ✅ Conversion history (Room-backed, toggle panel in top bar)

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
    - ✅ Calculation history (Room-backed, toggle panel in top bar)

---

## Phase 5: Info & Simple Tools — ~90% COMPLETE

17. **Battery** — ⚠️ PARTIAL
    - ✅ Real-time status, temperature (C/F), voltage, health
    - ❌ Charge session logging (track charge/discharge over time)
    - ❌ Estimated time to full/empty

18. **Network** — ✅ COMPLETE
    - ✅ Ping/latency test (8.8.8.8)
    - ✅ Connection type, IP address, WiFi signal strength
    - ✅ Connection history log (Room-backed, timestamped, toggle panel)

19. **Device Info** — ✅ COMPLETE
    - ✅ Grouped by category (Device, Software, Hardware, Display)
    - ✅ Copy all info to clipboard
    - ✅ Search/filter field (real-time filtering across all sections)

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

23. **Tally Counter** — ✅ COMPLETE
    - ✅ Named counters (multiple tallies with JSON persistence, swipe-to-delete)
    - ✅ Add counter dialog, per-counter increment/decrement/reset
    - ✅ Reset confirmation dialog
    - ✅ Foreground service notification
    - ✅ Backward-compatible migration from single-counter format

---

## Additional Features (outside original roadmap)

- ✅ **Notepad checklist enhancements** — subcategories with indent/outdent, auto-capitalize first word, changeable icons (5 styles), numbered top-level items, auto-collapsing header, Next button focus fix
- ✅ **Home screen** — scroll to favorites on app open
- ✅ **Notes camera capture** — take photos directly from notes attachment UI (alongside gallery picker), with runtime camera permission handling
- ✅ **CI workflow** — changed to manual-only trigger (workflow_dispatch) to save GitHub Actions minutes

---

## Remaining Work

### Larger features (not yet implemented):
- QR Code generation
- Color Picker palette export
- Battery charge session logging + time estimates

### Low priority / nice-to-have:
- Unit Converter favorites/pinned conversions

---

## Implementation Patterns (from notepad)
- Use `SwipeToDismissBox` for deletable list items (history, saved items)
- Use `LaunchedEffect` + `SnackbarHost` for undo flows
- Use `DataStore` for preferences, `Room` for structured data
- Use `UtilityTopBar` actions for share/export
- Keep voice input where text entry exists
- Use `rememberHaptic()` helper for haptic feedback
- Use `HistoryDao` with `featureKey` for per-tool Room-backed history
- Use JSON serialization in DataStore for complex state (stopwatch laps, tally counters)
