package ru.rpuxa.kotlincalculatorandroid.parcer

import ru.rpuxa.kotlincalculatorandroid.Settings
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Bracket
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Comma
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.*
import java.util.*
import kotlin.collections.ArrayList

class Expression : ArrayList<ExpressionPart>() {

    val stack = ArrayDeque<ExpressionPart>()
    var lastPart: ExpressionPart? = null
    var unaryMinusOpened = false

    fun addPart(part: ExpressionPart) {
        val stack = stack
        val lastPart = lastPart

        if (lastPart != null && part !is Comma && (lastPart !is Function && lastPart !is Comma && part !is Operator && part !is Bracket &&
                        !(lastPart is Bracket && lastPart.opened) || lastPart is Bracket &&
                        !lastPart.opened && part is Bracket && part.opened)) {
            addPart(multiply)
        }

        this.lastPart = part
        when (part) {
            is Number, is Variable, is Constant -> {
                add(part)
            }

            is Function -> {
                val functions = ArrayList<ExpressionPart>()
                while (!stack.isEmpty()) {
                    val stackPart = stack.peekLast()
                    if ((stackPart is Function && stackPart.priority > part.priority) || stackPart is Bracket)
                        break
                    stack.pollLast()
                    functions.add(stackPart)
                }
                addAll(functions)
                stack.addLast(part)
            }
            is Bracket -> {
                if (part.opened) {
                    stack.addLast(part)
                } else {
                    while (!stack.isEmpty()) {
                        val stackPart = stack.pollLast()
                        if (stackPart is Bracket && stackPart.opened)
                            break
                        else if (stack.isEmpty())
                            throw ParseException()
                        add(stackPart)
                    }
                    if (unaryMinusOpened) {
                        unaryMinusOpened = false
                        addPart(Bracket(false))
                    }
                }
            }

            is Comma -> {
                while (!stack.isEmpty()) {
                    val stackPart = stack.pollLast()
                    if (stackPart is Bracket && stackPart.opened) {
                        stack.addLast(stackPart)
                        break
                    }
                    else if (stack.isEmpty())
                        throw ParseException()
                    add(stackPart)
                }

        }
        }

        if (part == unaryMinus) {
            unaryMinusOpened = true
            addPart(Bracket(true))
        }
    }

    private fun toTree() {
        try {
            var i = 0
            while (i < size) {
                val func = get(i)
                if (func is Function) {
                    val args = ArrayList<Argument>()
                    repeat(func.argsCount) {

                        args.add(removeAt(i - func.argsCount) as Argument)
                    }
                    removeAt(i - func.argsCount)
                    add(i - func.argsCount, Func(args, func))
                    i = 0
                    continue
                }
                i++
            }
        } catch (e: Exception) {
            throw ParseException()
        }
        if (size != 1 || get(0) !is Argument)
            throw ParseException()
    }

    companion object {

        fun parse(string: String, isIntegral: Boolean = false): Argument {
            val expression = Expression()
            var ex = string.removeChar(' ')
            label@ while (!ex.isEmpty()) {
                for (begin in 0 until ex.length)
                    for (end in ex.length downTo begin) {
                        if (begin == end)
                            throw ParseException()
                        val substring = ex.substring(begin, end)
                        for (part in arrayOf(Function.getFunction(substring), Number.parse(substring),
                                Bracket.parse(substring), Variable.parse(substring), Constant.parse(substring),
                                Comma.parse(substring), if (isIntegral) Integrals.IntegralConstant.parse(substring) else null)) {
                            if (part == null)
                                continue
                            val lastPart = expression.lastPart
                            if (part is Number && !part.isMoreThenZero() && lastPart != null && (lastPart is Number || lastPart is Variable || (lastPart is Bracket && !lastPart.opened))) {
                                expression.addPart(minus)
                                part.negate()
                            } else if (part === minus && (lastPart == null || (lastPart !is Constant && lastPart !is Number && lastPart !is Variable && !(lastPart is Bracket && !lastPart.opened)))) {
                                expression.addPart(unaryMinus)
                                ex = ex.substring(end)
                                continue@label
                            }
                            expression.addPart(part)
                            ex = ex.substring(end)
                            continue@label
                        }
                    }
            }
            if (expression.unaryMinusOpened) {
                expression.unaryMinusOpened = false
                expression.addPart(Bracket(false))
            }

            while (!expression.stack.isEmpty())
                expression.add(expression.stack.pollLast())

            for ((i, arg) in expression.withIndex()) {
                if (arg == diff || (arg is Variable && arg !is Constant) || Settings.FormatNumber.rationalMode){
                    fullOptimize = false
                    break
                }
                if (i == expression.size - 1) {
                    fullOptimize = true
                    break
                }
            }

            expression.toTree()

            return expression[0] as Argument
        }


        private fun String.removeChar(char: Char): String {
            var string = this
            while (true) {
                val lastIndex = string.lastIndexOf(char)
                if (lastIndex >= 0)
                    string = string.substring(0, lastIndex) + string.substring(lastIndex + 1)
                else
                    break
            }
            return string
        }
    }
}
