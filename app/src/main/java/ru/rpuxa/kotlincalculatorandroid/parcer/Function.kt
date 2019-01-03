package ru.rpuxa.kotlincalculatorandroid.parcer

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Func
import java.io.Serializable
import java.math.BigDecimal

interface Function : ExpressionPart, Serializable {
    val name: String
    val argsCount: Int
    val priority: Int
        get() = 0
    val section: Int
    val argsString
        get() = "(x)"

    val description: String
        get() = NO_DESCRIPTION

    fun calculate(args: Array<Double>): Double

    fun calculate(args: Array<BigDecimal>) = BigDecimal(calculate(Array(args.size) { args[it].toDouble() }))

    fun diff(args: Array<Argument>): Argument

    fun func(vararg args: Argument) = Func(args.toArrayList(), this)

    companion object {

        fun getFunction(string: String): Function? {
            for (func in functions)
                if (func.name == string)
                    return func
            return null
        }

        val functions = arrayOf(
                diff, log, lg, cbrt, acot, cot,
                acos, asin, atan, ln, sin, sqrt, tan, cos,
                plus, multiply, minus, divide, power,
                ch, sh, cth, th, sgn, abs, definiteIntegral
        )

        val descriptionFunctions: Array<Function>
            get() = functions.filter { it.description != NO_DESCRIPTION }.toTypedArray()

    }
}

object FunctionSections {
    const val NONE = -1
    const val HYPERBOLIC = 3
    const val TRIGONOMETRY = 1
    const val DIFFERENTIAL = 2
    const val RADICAL_LOGARITHM = 0
    const val OTHER = 4

    //Не забывать!!!!!!!!!!!!!!!!!!!!!!!
    val NAMES = arrayOf(
            "Радикалы и логарифмы",
            "Тригонометрия",
            "Производные и интегралы",
            "Гиперболические функции",
            "Прочее"
    )
}


const val NO_DESCRIPTION = "\\\\\\no_description!!$$$$$$$$$$"

private fun <T> Array<out T>.toArrayList(): ArrayList<T> {
    val list = ArrayList<T>(this.size)
    for (i in this)
        list.add(i)
    return list
}
