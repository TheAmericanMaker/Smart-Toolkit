package com.smarttoolkit.app.feature.calculator

import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculatorEngine {

    fun evaluate(expression: String): String {
        return try {
            val tokens = tokenize(expression)
            val postfix = toPostfix(tokens)
            val result = evaluatePostfix(postfix)
            formatResult(result)
        } catch (e: ArithmeticException) {
            "Error: ${e.message ?: "Invalid result"}"
        } catch (_: Exception) {
            "Error"
        }
    }

    private sealed class Token {
        data class Num(val value: Double) : Token()
        data class Op(val op: String) : Token()
        data class Func(val name: String) : Token()
        data object LParen : Token()
        data object RParen : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val s = expr
            .replace(" ", "")
            .replace("\u00D7", "*")
            .replace("\u00F7", "/")
            .replace("\u03C0", Math.PI.toString())

        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(Token.Num(s.substring(start, i).toDouble()))
                }

                c == '(' -> {
                    tokens.add(Token.LParen)
                    i++
                }

                c == ')' -> {
                    tokens.add(Token.RParen)
                    i++
                }

                c in "+-*/^%" -> {
                    if (c == '-' && (tokens.isEmpty() || tokens.last() is Token.LParen || tokens.last() is Token.Op)) {
                        if (i + 1 < s.length && (s[i + 1].isDigit() || s[i + 1] == '.')) {
                            val start = i
                            i++
                            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                            tokens.add(Token.Num(s.substring(start, i).toDouble()))
                        } else {
                            tokens.add(Token.Num(0.0))
                            tokens.add(Token.Op("-"))
                            i++
                        }
                    } else {
                        tokens.add(Token.Op(c.toString()))
                        i++
                    }
                }

                c.isLetter() -> {
                    val start = i
                    while (i < s.length && s[i].isLetter()) i++
                    val name = s.substring(start, i)
                    when (name) {
                        "e" -> tokens.add(Token.Num(Math.E))
                        "pi" -> tokens.add(Token.Num(Math.PI))
                        else -> tokens.add(Token.Func(name))
                    }
                }

                else -> i++
            }
        }

        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/", "%" -> 2
        "^" -> 3
        else -> 0
    }

    private fun isRightAssociative(op: String): Boolean = op == "^"

    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Num -> output.add(token)
                is Token.Func -> stack.addLast(token)
                is Token.LParen -> stack.addLast(token)
                is Token.RParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty()) stack.removeLast()
                    if (stack.isNotEmpty() && stack.last() is Token.Func) output.add(stack.removeLast())
                }

                is Token.Op -> {
                    while (
                        stack.isNotEmpty() &&
                        stack.last() is Token.Op &&
                        (
                            precedence((stack.last() as Token.Op).op) > precedence(token.op) ||
                                (
                                    precedence((stack.last() as Token.Op).op) == precedence(token.op) &&
                                        !isRightAssociative(token.op)
                                    )
                            )
                    ) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }
            }
        }

        while (stack.isNotEmpty()) output.add(stack.removeLast())
        return output
    }

    private fun evaluatePostfix(tokens: List<Token>): Double {
        val stack = ArrayDeque<Double>()
        for (token in tokens) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
                is Token.Op -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val result = when (token.op) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b == 0.0) throw ArithmeticException("Division by zero") else a / b
                        "%" -> a % b
                        "^" -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: ${token.op}")
                    }
                    stack.addLast(validateResult(result))
                }

                is Token.Func -> {
                    val a = stack.removeLast()
                    val result = when (token.name) {
                        "sin" -> sin(Math.toRadians(a))
                        "cos" -> cos(Math.toRadians(a))
                        "tan" -> tan(Math.toRadians(a))
                        "log" -> {
                            if (a <= 0.0) throw ArithmeticException("Invalid input")
                            log10(a)
                        }

                        "ln" -> {
                            if (a <= 0.0) throw ArithmeticException("Invalid input")
                            ln(a)
                        }

                        "sqrt" -> {
                            if (a < 0.0) throw ArithmeticException("Invalid input")
                            sqrt(a)
                        }

                        else -> throw IllegalArgumentException("Unknown function: ${token.name}")
                    }
                    stack.addLast(validateResult(result))
                }

                else -> {}
            }
        }

        return stack.lastOrNull() ?: throw ArithmeticException("Invalid expression")
    }

    private fun validateResult(result: Double): Double {
        if (!result.isFinite()) {
            throw ArithmeticException("Invalid result")
        }
        return if (result == -0.0) 0.0 else result
    }

    private fun formatResult(result: Double): String {
        val normalized = validateResult(result)
        return if (normalized == normalized.toLong().toDouble()) {
            normalized.toLong().toString()
        } else {
            BigDecimal.valueOf(normalized).stripTrailingZeros().toPlainString()
        }
    }
}
