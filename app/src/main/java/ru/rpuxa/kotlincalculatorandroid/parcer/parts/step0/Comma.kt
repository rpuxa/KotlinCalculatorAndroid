package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0

import ru.rpuxa.kotlincalculatorandroid.parcer.ExpressionPart

class Comma : ExpressionPart {

    companion object {
        fun parse(string: String): Comma? {
            if (string.length != 1)
                return null
            return if (string[0] == ',') Comma() else null
        }

     //   fun parse(char: Char) = if (char == ',') Comma() else null
    }
}