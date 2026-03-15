# Smart Utilities — Feature Upgrade Plan

The notepad is now the gold standard: rich editing, persistence, undo, voice input, templates, export/import, smart categorization, swipe actions. The goal is to bring the other 21 utilities up in quality using the same patterns — without over-engineering the simpler tools.

---

## Phase 1: Cross-Cutting Improvements (all utilities benefit)

1. **Session persistence for stateful tools** — Tally Counter loses its count, Timer/Stopwatch reset on navigation. Use DataStore or Room to persist state so users don't lose work.
2. **Haptic feedback** — Only Tally Counter has it. Add tactile feedback to key actions (button presses, toggles, completions) across Calculator, Timer, Stopwatch, Random Generator.
3. **History / recent results** — Calculator, Unit Converter, Random Generator, and Tip Calculator all discard results. Add a scrollable history list (similar to notepad's note list) with copy/reuse.

---

## Phase 2: Sensor & Hardware Tools

4. **Stopwatch** — Add lap deletion, export laps to clipboard as formatted text, background persistence via notification so timing survives leaving the screen.
5. **Timer** — Add quick-select presets (1m, 5m, 10m, 15m, 30m, custom), repeat timer option, persistent countdown notification so it works in background.
6. **Compass** — Add accuracy indicator (sensor status), magnetic vs true north toggle with declination correction, lock bearing feature.
7. **Sound Meter** — Add min/max/avg tracking over a session, a scrolling history chart (like a simple line graph via Canvas), calibration offset setting.
8. **Bubble Level** — Add audible/haptic "level" feedback when surface is flat, improve calibration UX with guided instructions.

---

## Phase 3: Camera-Based Tools

9. **QR Scanner** — Add scan history (Room-backed, like notes list), with timestamps, swipe-to-delete, and quick re-open for URLs. Add generate QR code feature.
10. **Color Picker** — Add palette history (last N picked colors persisted), pick from gallery image, color name lookup, export palette.
11. **Magnifying Glass** — Add freeze/capture button to snapshot current view, preset zoom levels (2x, 4x, 8x chips).

---

## Phase 4: Computational Tools

12. **Calculator** — Show scrollable expression history (persisted per session), long-press result to copy, add percentage button in basic mode.
13. **Unit Converter** — Add favorites/recent conversions, swap animation, persist last-used category and units.
14. **Text Tools** — Add find & replace, word frequency count, remove duplicate lines, sort lines.
15. **Random Generator** — Add generation history, batch generate (e.g. 10 passwords at once), list shuffler mode (paste a list, get it shuffled).
16. **Tip Calculator** — Add rounding options (round up total / round up per person), tax input field, persist last tip percentage preference.

---

## Phase 5: Info & Simple Tools

17. **Battery** — Add charge session logging (track charge/discharge over time in a simple Room table), estimated time to full/empty.
18. **Network** — Add ping/latency test (to 8.8.8.8 or custom host), connection history log.
19. **Device Info** — Add grouping by category (Hardware, Software, Display, etc.), search/filter, individual item copy.
20. **Storage** — Add per-category breakdown (images, videos, apps, other), visual pie/donut chart.
21. **Flashlight** — Add SOS mode (auto-blink pattern), strobe with adjustable speed.
22. **Ruler** — Add device calibration flow (hold a known object like a credit card to calibrate), measurement save.
23. **Tally Counter** — Persist count across sessions, add named counters (multiple tallies like notes), reset confirmation dialog.

---

## Implementation Priority

**Start with Phase 1** — the cross-cutting changes (persistence, haptics, history) lay the groundwork and improve every tool at once.

Then tackle phases 2-5 in order of user impact. Each utility upgrade should follow the notepad patterns:
- Use `SwipeToDismissBox` for deletable list items (history, saved items)
- Use `LaunchedEffect` + `SnackbarHost` for undo flows
- Use `DataStore` for preferences, `Room` for structured data
- Use `UtilityTopBar` actions for share/export
- Keep voice input where text entry exists
