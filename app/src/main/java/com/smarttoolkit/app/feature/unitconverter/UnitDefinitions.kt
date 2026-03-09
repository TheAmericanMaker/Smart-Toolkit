package com.smarttoolkit.app.feature.unitconverter

data class UnitDef(
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

data class UnitCategory(
    val name: String,
    val units: List<UnitDef>
)

val unitCategories = listOf(
    UnitCategory("Length", listOf(
        UnitDef("Meter", "m", { it }, { it }),
        UnitDef("Kilometer", "km", { it * 1000 }, { it / 1000 }),
        UnitDef("Centimeter", "cm", { it / 100 }, { it * 100 }),
        UnitDef("Millimeter", "mm", { it / 1000 }, { it * 1000 }),
        UnitDef("Mile", "mi", { it * 1609.344 }, { it / 1609.344 }),
        UnitDef("Yard", "yd", { it * 0.9144 }, { it / 0.9144 }),
        UnitDef("Foot", "ft", { it * 0.3048 }, { it / 0.3048 }),
        UnitDef("Inch", "in", { it * 0.0254 }, { it / 0.0254 })
    )),
    UnitCategory("Weight", listOf(
        UnitDef("Kilogram", "kg", { it }, { it }),
        UnitDef("Gram", "g", { it / 1000 }, { it * 1000 }),
        UnitDef("Milligram", "mg", { it / 1_000_000 }, { it * 1_000_000 }),
        UnitDef("Pound", "lb", { it * 0.453592 }, { it / 0.453592 }),
        UnitDef("Ounce", "oz", { it * 0.0283495 }, { it / 0.0283495 }),
        UnitDef("Metric Ton", "t", { it * 1000 }, { it / 1000 })
    )),
    UnitCategory("Temperature", listOf(
        UnitDef("Celsius", "°C", { it }, { it }),
        UnitDef("Fahrenheit", "°F", { (it - 32) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32 }),
        UnitDef("Kelvin", "K", { it - 273.15 }, { it + 273.15 })
    )),
    UnitCategory("Volume", listOf(
        UnitDef("Liter", "L", { it }, { it }),
        UnitDef("Milliliter", "mL", { it / 1000 }, { it * 1000 }),
        UnitDef("Gallon (US)", "gal", { it * 3.78541 }, { it / 3.78541 }),
        UnitDef("Quart (US)", "qt", { it * 0.946353 }, { it / 0.946353 }),
        UnitDef("Cup (US)", "cup", { it * 0.236588 }, { it / 0.236588 }),
        UnitDef("Fluid Oz (US)", "fl oz", { it * 0.0295735 }, { it / 0.0295735 })
    )),
    UnitCategory("Speed", listOf(
        UnitDef("m/s", "m/s", { it }, { it }),
        UnitDef("km/h", "km/h", { it / 3.6 }, { it * 3.6 }),
        UnitDef("mph", "mph", { it * 0.44704 }, { it / 0.44704 }),
        UnitDef("Knots", "kn", { it * 0.514444 }, { it / 0.514444 })
    )),
    UnitCategory("Time", listOf(
        UnitDef("Second", "s", { it }, { it }),
        UnitDef("Minute", "min", { it * 60 }, { it / 60 }),
        UnitDef("Hour", "h", { it * 3600 }, { it / 3600 }),
        UnitDef("Day", "d", { it * 86400 }, { it / 86400 }),
        UnitDef("Week", "wk", { it * 604800 }, { it / 604800 })
    )),
    UnitCategory("Data", listOf(
        UnitDef("Byte", "B", { it }, { it }),
        UnitDef("Kilobyte", "KB", { it * 1024 }, { it / 1024 }),
        UnitDef("Megabyte", "MB", { it * 1_048_576 }, { it / 1_048_576 }),
        UnitDef("Gigabyte", "GB", { it * 1_073_741_824 }, { it / 1_073_741_824 }),
        UnitDef("Terabyte", "TB", { it * 1_099_511_627_776 }, { it / 1_099_511_627_776 })
    ))
)
