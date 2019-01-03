package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0

import ru.rpuxa.kotlincalculatorandroid.parcer.ExpressionPart

class Bracket(val opened: Boolean) : ExpressionPart {

    companion object {
        fun parse(string: String): Bracket? {
            if (string.length != 1)
                return null
            return when (string[0]) {
                '(' -> Bracket(true)
                ')' -> Bracket(false)
                else -> null
            }
        }

        fun parse(char: Char) : Bracket? {
            return when (char) {
                '(' -> Bracket(true)
                ')' -> Bracket(false)
                else -> null
            }
        }
    }
}