package com.smarttoolkit.app.feature.calculator

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun evaluatesOperatorPrecedence() {
        assertEquals("7", engine.evaluate("1+2*3"))
    }

    @Test
    fun supportsUnaryMinusBeforeParentheses() {
        assertEquals("-5", engine.evaluate("-(3+2)"))
    }

    @Test
    fun treatsExponentAsRightAssociative() {
        assertEquals("512", engine.evaluate("2^3^2"))
    }

    @Test
    fun returnsReadableErrorForInvalidScientificInput() {
        assertEquals("Error: Invalid input", engine.evaluate("sqrt(-1)"))
    }

    @Test
    fun formatsDecimalResultsIndependentlyOfSystemLocale() {
        val originalLocale = Locale.getDefault()

        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("2.5", engine.evaluate("10/4"))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
