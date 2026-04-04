package com.smarttoolkit.app.ui.guide

data class GuideSection(
    val title: String,
    val entries: List<GuideEntry>
)

data class GuideEntry(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)

val userGuideData: List<GuideSection> = listOf(
    GuideSection(
        title = "Getting Started",
        entries = listOf(
            GuideEntry(
                title = "Home Screen",
                content = "The home screen shows all 21 utilities in a grid layout. Tap any card to open that tool. Use the search bar at the top to quickly find a utility by name.",
                tags = listOf("home", "search", "grid", "launch")
            ),
            GuideEntry(
                title = "Favorites",
                content = "Tap the heart icon on any utility card to add it to your favorites. Favorites appear at the top of the home screen for quick access. Tap the reorder button in the Favorites header to rearrange them.",
                tags = listOf("favorites", "heart", "reorder", "pin")
            ),
            GuideEntry(
                title = "Settings",
                content = "Tap the gear icon on the home screen to open Settings. From here you can switch between light and dark mode, follow your system theme, or access this User Guide.",
                tags = listOf("settings", "dark mode", "theme", "appearance")
            )
        )
    ),
    GuideSection(
        title = "Flashlight",
        entries = listOf(
            GuideEntry(
                title = "Basic Use",
                content = "Tap the large flash icon to toggle the flashlight on or off. The button changes color to indicate the current state.",
                tags = listOf("flashlight", "torch", "light", "toggle")
            ),
            GuideEntry(
                title = "Modes",
                content = "Choose between three modes:\n\n• Steady \u2014 Constant light\n• SOS \u2014 Flashes the Morse code SOS pattern\n• Strobe \u2014 Rapid flashing (warning: may cause discomfort)",
                tags = listOf("flashlight", "sos", "strobe", "modes", "morse")
            )
        )
    ),
    GuideSection(
        title = "Stopwatch",
        entries = listOf(
            GuideEntry(
                title = "Basic Use",
                content = "Tap Start to begin timing, Stop to pause. Tap Reset to clear. The display shows hours, minutes, seconds, and milliseconds.",
                tags = listOf("stopwatch", "timer", "start", "stop", "reset")
            ),
            GuideEntry(
                title = "Laps",
                content = "Tap the Lap button while the stopwatch is running to record a split. Each lap shows both the split time and total elapsed time. Swipe a lap left to delete it. Tap the copy icon to copy all laps to clipboard.",
                tags = listOf("stopwatch", "lap", "split", "copy", "clipboard")
            )
        )
    ),
    GuideSection(
        title = "Timer",
        entries = listOf(
            GuideEntry(
                title = "Setting a Timer",
                content = "Enter hours, minutes, and seconds manually, or tap a quick preset (1m, 3m, 5m, 10m, 15m, 30m). Tap Start to begin the countdown.",
                tags = listOf("timer", "countdown", "preset", "alarm")
            ),
            GuideEntry(
                title = "Alarm Sound",
                content = "Tap the Sound button to choose an alarm tone. The picker shows all available alarm and notification sounds on your device. Tap the play icon to preview a sound before selecting it.",
                tags = listOf("timer", "alarm", "sound", "ringtone")
            ),
            GuideEntry(
                title = "Background Countdown",
                content = "The timer continues running even when you leave the app. A notification in your tray shows the remaining time with Pause and Cancel actions. When time is up, you will get a \"Time\u2019s Up!\" notification with a Dismiss button you can tap from anywhere.",
                tags = listOf("timer", "notification", "background", "dismiss", "foreground")
            )
        )
    ),
    GuideSection(
        title = "Calculator",
        entries = listOf(
            GuideEntry(
                title = "Basic Mode",
                content = "Tap numbers and operators to build an expression. Tap \u201c=\u201d to evaluate. Use C to clear all or the backspace button to delete the last character.",
                tags = listOf("calculator", "math", "add", "subtract", "multiply", "divide")
            ),
            GuideEntry(
                title = "Scientific Mode",
                content = "Tap the toggle button to switch to scientific mode. Additional functions include sin, cos, tan, log, ln, sqrt, exponent (^), pi (\u03c0), and e.",
                tags = listOf("calculator", "scientific", "sin", "cos", "tan", "log", "sqrt")
            ),
            GuideEntry(
                title = "History",
                content = "Tap the history icon to view past calculations. Tap any entry to restore it. Use the trash icon to clear history. Results are saved across sessions.",
                tags = listOf("calculator", "history", "restore")
            )
        )
    ),
    GuideSection(
        title = "Battery",
        entries = listOf(
            GuideEntry(
                title = "Battery Info",
                content = "View your battery percentage on a color-coded arc gauge (green, yellow, or red). Below it you will see detailed info: charging status, plug type, temperature, voltage, health, and battery technology. Tap the temperature card to switch between Celsius and Fahrenheit.",
                tags = listOf("battery", "charge", "temperature", "voltage", "health")
            )
        )
    ),
    GuideSection(
        title = "Compass",
        entries = listOf(
            GuideEntry(
                title = "Reading the Compass",
                content = "The compass rose rotates in real time to show magnetic north. The current heading is displayed in degrees along with the cardinal direction (N, NE, E, etc.).",
                tags = listOf("compass", "north", "heading", "direction", "bearing")
            ),
            GuideEntry(
                title = "Bearing Lock",
                content = "Tap the lock icon to freeze the current bearing. A marker will appear on the compass rose showing the locked direction. Tap again to unlock.",
                tags = listOf("compass", "lock", "bearing")
            ),
            GuideEntry(
                title = "Calibration",
                content = "If the accuracy indicator shows Low or Unreliable, move your phone slowly in a figure-8 pattern to recalibrate the magnetometer.",
                tags = listOf("compass", "calibration", "accuracy", "figure-8")
            )
        )
    ),
    GuideSection(
        title = "Network",
        entries = listOf(
            GuideEntry(
                title = "Network Info",
                content = "View your current connection type, IP address, link speed, frequency, and signal strength (shown as bars). Tap Refresh to update the info.",
                tags = listOf("network", "wifi", "ip", "signal", "speed")
            ),
            GuideEntry(
                title = "Ping Test",
                content = "Tap the Ping button to measure round-trip latency to Google DNS (8.8.8.8). The result is shown in milliseconds.",
                tags = listOf("network", "ping", "latency", "dns")
            )
        )
    ),
    GuideSection(
        title = "Storage",
        entries = listOf(
            GuideEntry(
                title = "Storage Info",
                content = "See internal storage usage on a color-coded arc gauge. Below it, progress bars show used, available, and total space for both internal and external storage. Tap Refresh to update.",
                tags = listOf("storage", "disk", "space", "internal", "external", "sd card")
            )
        )
    ),
    GuideSection(
        title = "Ruler",
        entries = listOf(
            GuideEntry(
                title = "Measuring",
                content = "A ruler is drawn along the screen edge with precise tick marks. Toggle between metric (cm/mm) and imperial (inches) with the unit button.",
                tags = listOf("ruler", "measure", "cm", "inches", "length")
            ),
            GuideEntry(
                title = "Calibration",
                content = "Tap the settings icon to open the calibration panel. Adjust the DPI offset slider until the ruler matches a physical ruler or known measurement. This compensates for screen density variations.",
                tags = listOf("ruler", "calibration", "dpi", "offset", "accuracy")
            )
        )
    ),
    GuideSection(
        title = "QR Scanner",
        entries = listOf(
            GuideEntry(
                title = "Scanning",
                content = "Point your camera at a QR code or barcode. The app detects and decodes it automatically. The result is displayed in a card with a Copy button. If the result is a URL, an Open button appears too.",
                tags = listOf("qr", "barcode", "scan", "camera", "url", "copy")
            ),
            GuideEntry(
                title = "Scan History",
                content = "Tap the history icon to view previously scanned codes with timestamps. Swipe any entry left to delete it. Use Clear All to remove the entire history.",
                tags = listOf("qr", "history", "delete", "clear")
            )
        )
    ),
    GuideSection(
        title = "Unit Converter",
        entries = listOf(
            GuideEntry(
                title = "Converting Units",
                content = "Select a category (Length, Weight, Temperature, etc.) from the tabs at the top. Choose \"From\" and \"To\" units from the dropdown menus, then type a value. The conversion updates in real time. Tap the swap button to flip the direction. Tap Copy to copy the result.",
                tags = listOf("unit", "convert", "length", "weight", "temperature", "volume")
            )
        )
    ),
    GuideSection(
        title = "Text Tools",
        entries = listOf(
            GuideEntry(
                title = "Text Transforms",
                content = "Type or paste text in the input area. Tap a transform chip to apply it:\n\n• UPPERCASE / lowercase / Title Case\n• Reverse text\n• Trim extra spaces\n• Deduplicate lines\n• Sort lines alphabetically",
                tags = listOf("text", "uppercase", "lowercase", "reverse", "trim", "sort", "transform")
            ),
            GuideEntry(
                title = "Find & Replace",
                content = "Tap the Find & Replace chip to open a search panel. Enter a search term and a replacement, then apply. A match counter shows how many occurrences are found.",
                tags = listOf("text", "find", "replace", "search")
            ),
            GuideEntry(
                title = "Word Frequency",
                content = "Tap the Word Freq chip to see the top 30 most used words in your text, sorted by count.",
                tags = listOf("text", "word", "frequency", "count", "stats")
            ),
            GuideEntry(
                title = "Stats",
                content = "Real-time counts for characters, words, sentences, and lines are displayed below the input area.",
                tags = listOf("text", "character", "word", "count", "stats")
            )
        )
    ),
    GuideSection(
        title = "Random Generator",
        entries = listOf(
            GuideEntry(
                title = "Modes",
                content = "Switch between four modes using the tabs:\n\n• Number \u2014 Generate a random integer in a min/max range\n• Dice \u2014 Roll a 6-sided die\n• Coin \u2014 Flip a coin (Heads or Tails)\n• Password \u2014 Generate a secure password",
                tags = listOf("random", "number", "dice", "coin", "password")
            ),
            GuideEntry(
                title = "Password Generator",
                content = "Set the password length and toggle which character sets to include: uppercase (A\u2013Z), lowercase (a\u2013z), digits (0\u20139), and symbols. Tap Generate, then Copy to grab the result.",
                tags = listOf("random", "password", "generate", "secure", "symbols")
            ),
            GuideEntry(
                title = "Batch Generation",
                content = "Enter a batch count greater than 1 to generate multiple results at once. Use the history button to review past results.",
                tags = listOf("random", "batch", "history", "multiple")
            )
        )
    ),
    GuideSection(
        title = "Notepad",
        entries = listOf(
            GuideEntry(
                title = "Creating Notes",
                content = "Tap the + button to create a new note. Choose between a text note or a checklist. Enter a title and content, then press Back to auto-save.",
                tags = listOf("notepad", "note", "create", "text", "checklist", "save")
            ),
            GuideEntry(
                title = "Organizing Notes",
                content = "Tap the pin icon to keep important notes at the top. Use color circles in the editor to color-code notes. Use the filter chips (All, Notes, Checklists) to narrow the list. Search by tapping the search icon.",
                tags = listOf("notepad", "pin", "color", "filter", "search", "organize")
            ),
            GuideEntry(
                title = "Voice Input & Images",
                content = "Tap the microphone icon next to the title or content field to dictate text. The first time you use voice input, Smart Toolkit explains that speech recognition may be handled by your device or its configured provider. Tap the image button to attach a photo. Tap an attached image to view it fullscreen with OCR text extraction.",
                tags = listOf("notepad", "voice", "dictation", "image", "photo", "ocr")
            ),
            GuideEntry(
                title = "Export & Import",
                content = "From the note list, tap the three-dot menu to Export (saves all notes as a ZIP file) or Import (restores from a previously exported ZIP). If Android device backup is enabled, note attachments may also be included in your device's backup or transfer flow.",
                tags = listOf("notepad", "export", "import", "backup", "zip")
            )
        )
    ),
    GuideSection(
        title = "Device Info",
        entries = listOf(
            GuideEntry(
                title = "Viewing Device Info",
                content = "Browse hardware, software, and system details organized into cards. Tap the copy icon in the top bar to copy all information to clipboard for sharing or troubleshooting.",
                tags = listOf("device", "info", "hardware", "software", "system", "copy")
            )
        )
    ),
    GuideSection(
        title = "Sound Meter",
        entries = listOf(
            GuideEntry(
                title = "Measuring Sound",
                content = "Tap Start to begin measuring ambient sound in decibels. The gauge shows the current level with color coding: green (quiet), yellow (moderate), red (loud). Min and max readings are tracked throughout the session.",
                tags = listOf("sound", "meter", "decibel", "db", "noise", "measure")
            ),
            GuideEntry(
                title = "Waveform & Export",
                content = "A live waveform chart shows the last 100 readings with gridlines at 30, 60, and 90 dB. Tap Export to save the session as a CSV file, or Share to send it.",
                tags = listOf("sound", "waveform", "export", "csv", "share", "chart")
            )
        )
    ),
    GuideSection(
        title = "Bubble Level",
        entries = listOf(
            GuideEntry(
                title = "Using the Level",
                content = "The app shows two levels: a circular surface level (for flat surfaces) and a tube side level. The bubble turns green and your phone vibrates when the surface is level. Pitch and roll angles are shown in degrees.",
                tags = listOf("bubble", "level", "surface", "pitch", "roll", "flat")
            ),
            GuideEntry(
                title = "Calibration",
                content = "Place your device on a known flat surface and tap Calibrate. This zeroes out any sensor offset so measurements are accurate.",
                tags = listOf("bubble", "level", "calibrate", "zero", "flat")
            )
        )
    ),
    GuideSection(
        title = "Tip Calculator",
        entries = listOf(
            GuideEntry(
                title = "Calculating Tips",
                content = "Enter the bill amount and optional tax. Choose a tip percentage from the presets (10%, 15%, 18%, 20%) or enter a custom amount. The result card shows the tip amount and total. Adjust the number of people to split the bill evenly.",
                tags = listOf("tip", "calculator", "bill", "split", "percent", "restaurant")
            ),
            GuideEntry(
                title = "Rounding",
                content = "Choose a rounding mode: None (exact), Round Total (rounds the final total), or Round Per Person (rounds each person\u2019s share).",
                tags = listOf("tip", "rounding", "total", "per person")
            )
        )
    ),
    GuideSection(
        title = "Tally Counter",
        entries = listOf(
            GuideEntry(
                title = "Counting",
                content = "Tap + to increment and \u2013 to decrement. The count is saved automatically and persists across app restarts. Tap Reset to zero out the counter (you will be asked to confirm).",
                tags = listOf("tally", "counter", "count", "increment", "decrement", "reset")
            )
        )
    ),
    GuideSection(
        title = "Magnifier",
        entries = listOf(
            GuideEntry(
                title = "Zooming",
                content = "The camera feed is shown with adjustable magnification. Use the preset chips (2x, 4x, 6x, 8x), the +/\u2013 buttons, or the slider to control zoom. Tap the flash icon in the top bar to toggle the flashlight for extra illumination.",
                tags = listOf("magnifier", "zoom", "camera", "flash", "magnifying glass")
            )
        )
    ),
    GuideSection(
        title = "Color Picker",
        entries = listOf(
            GuideEntry(
                title = "Picking Colors",
                content = "Point your camera at any surface. The crosshair in the center samples the color in real time. The color preview circle and info card show the hex value, RGB, and HSL components.",
                tags = listOf("color", "picker", "hex", "rgb", "hsl", "camera", "sample")
            ),
            GuideEntry(
                title = "Color Palette",
                content = "Tap Save to add the current color to your palette (up to 20 colors). Tap any saved swatch to copy its hex value. Use Clear Palette to remove all saved colors.",
                tags = listOf("color", "palette", "save", "swatch", "copy", "hex")
            )
        )
    ),
    GuideSection(
        title = "Permissions",
        entries = listOf(
            GuideEntry(
                title = "Camera",
                content = "Required by QR Scanner, Magnifier, and Color Picker. You will be prompted the first time you open one of these tools. You can manage this in your device\u2019s app settings.",
                tags = listOf("permission", "camera")
            ),
            GuideEntry(
                title = "Microphone",
                content = "Required by Sound Meter and Notepad voice input. Grant access when prompted to enable audio recording and dictation. Depending on your device, Notepad voice input may use your configured speech recognition provider.",
                tags = listOf("permission", "microphone", "audio", "voice")
            ),
            GuideEntry(
                title = "Notifications",
                content = "Required for the Timer\u2019s background countdown and alarm notifications. On Android 13+, you will be asked to allow notifications the first time you start a timer.",
                tags = listOf("permission", "notification", "timer", "alarm")
            )
        )
    )
)
