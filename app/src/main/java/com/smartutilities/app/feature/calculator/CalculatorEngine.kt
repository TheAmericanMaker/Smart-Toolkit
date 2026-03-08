package com.smartutilities.app.feature.calculator

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
            if (result == result.toLong().toDouble()) result.toLong().toString()
            else "%.10g".format(result)
        } catch (e: ArithmeticException) {
            "Error: ${e.message}"
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
        val s = expr.replace(" ", "").replace("×", "*").replace("÷", "/").replace("π", "3.14159265358979")

        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(Token.Num(s.substring(start, i).toDouble()))
                }
                c == '(' -> { tokens.add(Token.LParen); i++ }
                c == ')' -> { tokens.add(Token.RParen); i++ }
                c in "+-*/^%" -> {
                    if (c == '-' && (tokens.isEmpty() || tokens.last() is Token.LParen || tokens.last() is Token.Op)) {
                        val start = i
                        i++
                        while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                        tokens.add(Token.Num(s.substring(start, i).toDouble()))
                    } else {
                        tokens.add(Token.Op(c.toString())); i++
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
                    while (stack.isNotEmpty() && stack.last() is Token.Op &&
                        precedence((stack.last() as Token.Op).op) >= precedence(token.op)) {
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
                    stack.addLast(when (token.op) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b == 0.0) throw ArithmeticException("Division by zero") else a / b
                        "%" -> a % b
                        "^" -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: ${token.op}")
                    })
                }
                is Token.Func -> {
                    val a = stack.removeLast()
                    stack.addLast(when (token.name) {
                        "sin" -> sin(Math.toRadians(a))
                        "cos" -> cos(Math.toRadians(a))
                        "tan" -> tan(Math.toRadians(a))
                        "log" -> log10(a)
                        "ln" -> ln(a)
                        "sqrt" -> sqrt(a)
                        else -> throw IllegalArgumentException("Unknown function: ${token.name}")
                    })
                }
                else -> {}
            }
        }
        return stack.last()
    }
}
