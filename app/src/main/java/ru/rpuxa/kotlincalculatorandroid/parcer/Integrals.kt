package ru.rpuxa.kotlincalculatorandroid.parcer

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.MultiOperator
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.Permutator
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

object Integrals {
    private class Integral(val from: Argument, val to: Argument)

    fun definiteIntegral(args: ArrayList<Argument>): BigDecimal? {
        val argument1 = args[1]
        val argument2 = args[2]
        val b = when (argument2) {
            is Number -> argument2.value
            is Constant -> argument2.value
            else -> return null
        }
        val a = when (argument1) {
            is Number -> argument1.value
            is Constant -> argument1.value
            else -> return null
        }
        if (b < a)
            return null
        val func = { x: BigDecimal ->
            args[0].calculateWithVar("x", x)
        }
        val segments = 1000
        var step1 = BigDecimal.ZERO.setScale(16)
        var step2 = BigDecimal.ZERO.setScale(16)
        val r = (b - a) / BigDecimal(segments).setScale(16)
        for (i in 1 until segments) {
            val n = r * BigDecimal(i).setScale(16) + a
            if (i and 1 == 0)
                step2 += func(n)
            else
                step1 += func(n)
        }
        return ((b - a) / BigDecimal(3 * segments).setScale(16) * ((func(a) + func(b)) +
                BigDecimal(4).setScale(16) * step1 + BigDecimal(2).setScale(16) * step2)).setScale(3, RoundingMode.HALF_EVEN)
    }

    fun integrate(argument: Argument): Argument? {
        if (argument is Func) {
            when (argument.impl) {
                summa -> {
                    val args = Args()
                    for (arg in argument.args) {
                        args.add(integrate(arg) ?: return null)
                    }
                    return Func(args, summa)
                }

                product -> {
                    val argumentCopied = argument.copy() as Func
                    val numbers = Args()
                    for (i in argumentCopied.args.size - 1 downTo 0) {
                        val arg = argumentCopied.args[i]
                        if (arg is Number) {
                            numbers.add(arg)
                            argumentCopied.args.removeAt(i)
                        }
                    }

                    return product.func(integrate(argumentCopied)
                            ?: return null, *(numbers.toTypedArray()))
                }
            }
        }

        for (integrals in arrayOf(Templates.integralWithoutAll, Templates.integralWithoutMemb, Templates.integralWithoutMult, Templates.integrals))
            for (integral in integrals) {
                val map = TreeMap<IntegralConstant, BigDecimal>()
                if (isTemplate(argument, integral.from, map)) {
                    val argument1 = integral.to
                    return replaceIntegralConstants(argument1, map).optimize()
                }
            }

        return null
    }

    private fun isTemplate(argument: Argument, template: Argument, map: TreeMap<IntegralConstant, BigDecimal>): Boolean {
        if (template is Number && argument is Number && template.value.compareTo(argument.value) == 0)
            return true
        if (template is Variable && argument is Variable && template.name == argument.name)
            return true
        if (template is IntegralConstant) {
            val value = when (argument) {
                is Constant -> argument.value
                is Number -> argument.value
                else -> return false
            }
            val n = map[template]
            if (n == null) {
                map[template] = value
                return true
            }
            return n.compareTo(value) == 0
        }
        if (template !is Func || argument !is Func)
            return false
        if (template.impl != argument.impl || template.args.size != argument.args.size)
            return false
        if (template.impl is MultiOperator) {
            var same = false
            Permutator(argument.args.toTypedArray()) {
                for (i in template.args.indices)
                    if (!isTemplate(argument.args[i], template.args[i], map))
                        return@Permutator false
                same = true
                return@Permutator true
            }
            return same
        }
        for (i in template.args.indices) {
            if (!isTemplate(argument.args[i], template.args[i], map))
                return false
        }
        return true
    }

    private fun replaceIntegralConstants(argument: Argument, map: TreeMap<IntegralConstant, BigDecimal>): Argument = when (argument) {
        is IntegralConstant -> {
            val v = map[argument] ?: throw ParseException()
            Number(v)
        }
        is Func -> {
            val args = Args()
            for (arg in argument.args) {
                args.add(replaceIntegralConstants(arg, map))
            }
            Func(args, argument.impl)
        }
        else -> argument
    }

    private fun replaceVariable(argument: Argument): Argument = when (argument) {
        is Variable -> if (argument.name == "x")
            summa.func(product.func(IntegralConstant("mult"), Variable("x")), IntegralConstant("memb"))
        else
            argument
        is Func -> {
            val args = Args()
            for (arg in argument.args) {
                args.add(replaceVariable(arg))
            }
            Func(args, argument.impl)
        }
        else -> argument
    }

    private fun replaceVariableWithoutMult(argument: Argument): Argument = when (argument) {
        is Variable -> if (argument.name == "x")
            summa.func(Variable("x"), IntegralConstant("memb"))
        else
            argument
        is Func -> {
            val args = Args()
            for (arg in argument.args) {
                args.add(replaceVariableWithoutMult(arg))
            }
            Func(args, argument.impl)
        }
        else -> argument
    }

    private fun replaceVariableWithoutMemb(argument: Argument): Argument = when (argument) {
        is Variable -> if (argument.name == "x")
            product.func(IntegralConstant("mult"), Variable("x"))
        else
            argument
        is Func -> {
            val args = Args()
            for (arg in argument.args) {
                args.add(replaceVariableWithoutMemb(arg))
            }
            Func(args, argument.impl)
        }
        else -> argument
    }


    class IntegralConstant(name: String) : Variable(name), Comparable<IntegralConstant> {
        override fun compareTo(other: IntegralConstant): Int {
            return name.compareTo(other.name)
        }

        override fun equals(other: Any?): Boolean {
            if (other === this)
                return true
            if (other == null || other !is IntegralConstant)
                return false

            return other.name == name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun toString(): String {
            throw ParseException()
        }

        companion object {
            fun parse(string: String): IntegralConstant? {
                if (string.length != 1)
                    return null
                val c = string[0]
                if (c !in 'a'..'d')
                    return null
                return IntegralConstant("$c")
            }
        }
    }


    private object Templates {
        val template = arrayOf(


                /**
                 *
                 *
                 *
                 * ВСЕ ШАБЛОНЫ ИНТЕГРАЛОВ
                 */


                "a", "ax"
              /*  "x ^ a", "x ^ (a + 1) / (a + 1)",
                "1 / (a^2 + x^2)", "atan(x / a) / a",
                "1 / (x^ 2 - a^2)", "ln(abs((x - a) / (x + a))) / (2a)",
                "ln(x)", "xln(x) - x",
                "1 / (x ln(x))", "ln(abs(ln(x)))",
                "e ^ x", "e ^ x",
                "a ^ x", "a ^ x / ln(a)",
                "sin(x)", "-cos(x)",
                "cos(x)", "sin(x)"*/



                /**
                 * КОНЕЦ
                 *
                 *
                 *
                 *
                 */


        )


        val integrals = Array(template.size / 2) {
            Integral(
                    replaceVariable(Expression.parse(template[it * 2], true).optimize()),
                    replaceVariable(Expression.parse(template[it * 2 + 1], true).optimize()) / IntegralConstant("mult")

            )
        }

        val integralWithoutMult = Array(template.size / 2) {
            Integral(
                    replaceVariableWithoutMult(Expression.parse(template[it * 2], true).optimize()),
                    replaceVariableWithoutMult(Expression.parse(template[it * 2 + 1], true).optimize())

            )
        }

        val integralWithoutMemb = Array(template.size / 2) {
            Integral(
                    replaceVariableWithoutMemb(Expression.parse(template[it * 2], true).optimize()),
                    replaceVariableWithoutMemb(Expression.parse(template[it * 2 + 1], true).optimize()) / IntegralConstant("mult")

            )
        }

        val integralWithoutAll = Array(template.size / 2) {
            Integral(
                    Expression.parse(template[it * 2], true).optimize(),
                    Expression.parse(template[it * 2 + 1], true).optimize()
            )
        }
    }
}




