package com.smarttoolkit.app.feature.colorpicker

object ColorNameLookup {
    private data class NamedColor(val name: String, val r: Int, val g: Int, val b: Int)

    private val colors = listOf(
        NamedColor("Black", 0, 0, 0),
        NamedColor("White", 255, 255, 255),
        NamedColor("Red", 255, 0, 0),
        NamedColor("Green", 0, 128, 0),
        NamedColor("Blue", 0, 0, 255),
        NamedColor("Yellow", 255, 255, 0),
        NamedColor("Cyan", 0, 255, 255),
        NamedColor("Magenta", 255, 0, 255),
        NamedColor("Orange", 255, 165, 0),
        NamedColor("Pink", 255, 192, 203),
        NamedColor("Purple", 128, 0, 128),
        NamedColor("Brown", 139, 69, 19),
        NamedColor("Gray", 128, 128, 128),
        NamedColor("Navy", 0, 0, 128),
        NamedColor("Teal", 0, 128, 128),
        NamedColor("Maroon", 128, 0, 0),
        NamedColor("Olive", 128, 128, 0),
        NamedColor("Lime", 0, 255, 0),
        NamedColor("Coral", 255, 127, 80),
        NamedColor("Salmon", 250, 128, 114),
        NamedColor("Turquoise", 64, 224, 208),
        NamedColor("Indigo", 75, 0, 130),
        NamedColor("Violet", 238, 130, 238),
        NamedColor("Gold", 255, 215, 0),
        NamedColor("Silver", 192, 192, 192),
        NamedColor("Crimson", 220, 20, 60),
        NamedColor("Tomato", 255, 99, 71),
        NamedColor("Sky Blue", 135, 206, 235),
        NamedColor("Royal Blue", 65, 105, 225),
        NamedColor("Forest Green", 34, 139, 34),
        NamedColor("Sea Green", 46, 139, 87),
        NamedColor("Slate Gray", 112, 128, 144),
        NamedColor("Dark Gray", 169, 169, 169),
        NamedColor("Light Gray", 211, 211, 211),
        NamedColor("Ivory", 255, 255, 240),
        NamedColor("Beige", 245, 245, 220),
        NamedColor("Khaki", 240, 230, 140),
        NamedColor("Lavender", 230, 230, 250),
        NamedColor("Plum", 221, 160, 221),
        NamedColor("Orchid", 218, 112, 214),
        NamedColor("Tan", 210, 180, 140),
        NamedColor("Sienna", 160, 82, 45),
        NamedColor("Chocolate", 210, 105, 30),
        NamedColor("Peru", 205, 133, 63),
        NamedColor("Firebrick", 178, 34, 34),
        NamedColor("Dark Red", 139, 0, 0),
        NamedColor("Hot Pink", 255, 105, 180),
        NamedColor("Deep Pink", 255, 20, 147),
        NamedColor("Medium Purple", 147, 112, 219),
        NamedColor("Steel Blue", 70, 130, 180),
        NamedColor("Cadet Blue", 95, 158, 160),
        NamedColor("Dark Olive Green", 85, 107, 47),
        NamedColor("Dark Sea Green", 143, 188, 143),
        NamedColor("Light Coral", 240, 128, 128),
        NamedColor("Dark Salmon", 233, 150, 122)
    )

    fun findNearest(r: Int, g: Int, b: Int): String {
        return colors.minByOrNull { c ->
            val dr = c.r - r; val dg = c.g - g; val db = c.b - b
            dr * dr + dg * dg + db * db
        }?.name ?: "Unknown"
    }
}
