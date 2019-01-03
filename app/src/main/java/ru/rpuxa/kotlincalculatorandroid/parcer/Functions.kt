package ru.rpuxa.kotlincalculatorandroid.parcer

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.MultiOperator
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Func
import ru.rpuxa.kotlincalculatorandroid.parcer.types.Hyperbolic
import ru.rpuxa.kotlincalculatorandroid.parcer.types.OtherFunction
import ru.rpuxa.kotlincalculatorandroid.parcer.types.RadicalsLogarithms
import ru.rpuxa.kotlincalculatorandroid.parcer.types.Trigonometry
import java.math.BigDecimal
import kotlin.math.acosh
import kotlin.math.asinh
import kotlin.math.atanh

/**
 * Производные
 */


val definiteIntegral = object : Function {
    override val name = "∫ab"
    override val argsCount = 3
    override val priority = 0
    override val section = FunctionSections.DIFFERENTIAL
    override val argsString = "(function, a, b)"

    override val description: String
        get() = "Определенный интеграл, площадь под графиком\n" +
                "function - подынтегральная функция\n" +
                "a - нижний предел\n" +
                "b - верхний предел"

    override fun diff(args: Array<Argument>): Argument {
        throw ParseException()
    }

    override fun calculate(args: Array<Double>): Double {
        throw ParseException()
    }

}

val diff = object : Function {
    override val name = "dx"
    override val argsCount = 1
    override val priority = 0
    override val section: Int
        get() = FunctionSections.DIFFERENTIAL

    override val description: String
        get() = "Производная функции"

    override fun diff(args: Array<Argument>): Argument {
        throw ParseException()
    }

    override fun calculate(args: Array<Double>): Double {
        throw ParseException()
    }
}


/**
 *  Гиперболические
 */


val arch = object : Hyperbolic {
    override val name = "arch"

    override val description: String
        get() = "Гиперболический арккосинус. Ареакосинус"

    override fun calculate(args: Array<Double>) = acosh(args[0])

    override fun diff(args: Array<Argument>) = (((args[0] pow Number(2.0)) - Number(1.0)) pow Number(-.5)) * args[0].diff()
}

val arcth = object : Hyperbolic {
    override val name = "arcth"

    override val description: String
        get() = "Гиперболический арккотангенс. Ареакотангенс"

    override fun calculate(args: Array<Double>) = atanh(1 / args[0])

    override fun diff(args: Array<Argument>) = ((Number(1.0) - (args[0] pow Number(2.0))) pow Number(-1.0)) * args[0].diff()

}

val arsh = object : Hyperbolic {
    override val name = "arsh"

    override val description: String
        get() = "Гиперболический арксинус. Ареасинус"

    override fun calculate(args: Array<Double>) = asinh(args[0])

    override fun diff(args: Array<Argument>) = (((args[0] pow Number(2.0)) + Number(1.0)) pow Number(-.5)) * args[0].diff()

}

val arth = object : Hyperbolic {
    override val name = "arth"

    override val description: String
        get() = "Гиперболический арктангенс. Ареатангенс"

    override fun calculate(args: Array<Double>) = atanh(args[0])

    override fun diff(args: Array<Argument>) = ((Number(1.0) - (args[0] pow Number(2.0))) pow Number(-1.0)) * args[0].diff()

}

val ch: Hyperbolic = object : Hyperbolic {
    override val name = "ch"

    override val description: String
        get() = "Гиперболический косинус"

    override fun calculate(args: Array<Double>) = Math.cosh(args[0])

    override fun diff(args: Array<Argument>) = sh.func(args[0]) * args[0].diff()

}

val cth = object : Hyperbolic {
    override val name = "cth"

    override val description: String
        get() = "Гиперболический котангенс"

    override fun calculate(args: Array<Double>) = 1 / Math.tanh(args[0])

    override fun diff(args: Array<Argument>) = -(sh.func(args[0]) pow Number(-2.0)) * args[0].diff()

}

val sh = object : Hyperbolic {
    override val name = "sh"

    override val description: String
        get() = "Гиперболический синус"

    override fun calculate(args: Array<Double>) = Math.sinh(args[0])

    override fun diff(args: Array<Argument>) = ch.func(args[0]) * args[0].diff()

}

val th = object : Hyperbolic {
    override val name = "th"

    override val description: String
        get() = "Гиперболический тангенс"

    override fun calculate(args: Array<Double>) = Math.tanh(args[0])

    override fun diff(args: Array<Argument>) = (ch.func(args[0]) pow Number(-2.0)) * args[0].diff()

}


/**
 *  Операторы
 */


val divide: Operator = object : Operator {
    override val name = "/"
    override val priority = 9

    override fun calculate(args: Array<Double>) = args[0] / args[1]

    override fun calculate(args: Array<BigDecimal>) = args[0] / args[1]

    override fun diff(args: Array<Argument>) = (args[0].diff() * args[1] - args[0] * args[1].diff()) / (args[1] pow Number(2.0))
}

val minus: Operator = object : Operator {
    override val priority = 10
    override val name = "-"

    override fun calculate(args: Array<Double>) = args[0] - args[1]

    override fun calculate(args: Array<BigDecimal>) = args[0] - args[1]

    override fun diff(args: Array<Argument>) = args[0].diff() - args[1].diff()
}

val multiply = object : Operator {
    override val priority = 9
    override val name = "*"

    override fun calculate(args: Array<Double>) = args[1] * args[0]

    override fun calculate(args: Array<BigDecimal>) = args[0] * args[1]

    override fun diff(args: Array<Argument>) = args[0].diff() * args[1] + args[0] * args[1].diff()
}

val plus = object : Operator {
    override val priority = 10
    override val name = "+"

    override fun calculate(args: Array<Double>) = args[0] + args[1]

    override fun calculate(args: Array<BigDecimal>) = args[0] + args[1]

    override fun diff(args: Array<Argument>) = args[0].diff() + args[1].diff()
}

val power: Operator = object : Operator {
    override val priority = 8
    override val name = "^"

    override fun calculate(args: Array<Double>): Double {
        if (args[0] == 0.0 && args[1] == -1.0)
            throw ParseException()
        return Math.pow(args[0], args[1])
    }

    /*   override fun calculate(args: Array<BigDecimal>): BigDecimal {
           val arg = Apfloat(args[0])
           val answer = ApfloatMath.pow(arg, Apfloat(args[0]))
           ApfloatMath.
           return
       }*/

    override fun diff(args: Array<Argument>) = (args[0] pow args[1]) * (args[1] * args[0].diff() + args[0] * ln.func(args[0]) * args[1].diff()) / args[0]
}


/**
 * Другие
 */


val abs = object : OtherFunction {

    override val name = "abs"
    override val argsCount = 1
    override val description: String
        get() = "Модуль числа"

    override fun calculate(args: Array<Double>) = Math.abs(args[0])

    override fun calculate(args: Array<BigDecimal>) = args[0].abs()!!

    override fun diff(args: Array<Argument>) = sgn.func(args[0]) * args[0].diff()

}

val sgn = object : OtherFunction {

    override val name = "sgn"
    override val argsCount = 1
    override val description: String
        get() = """Знак числа.
            | при x < 0, возвращает -1
            | при x = 0, возвращает 0
            | при x > 0, возвращает 1
        """.trimMargin()

    override fun calculate(args: Array<Double>) = Math.signum(args[0])

    override fun calculate(args: Array<BigDecimal>) = BigDecimal(args[0].signum())

    override fun diff(args: Array<Argument>) = Number(0)

}


/**
 * Радикалы и логарифмы
 */


val cbrt = object : RadicalsLogarithms {
    override val name = "∛"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Кубический корень"

    override fun calculate(args: Array<Double>): Double {
        return Math.cbrt(args[0])
    }

    override fun diff(args: Array<Argument>): Argument {
        return Number(1.0 / 3) * (args[0] pow Number(-2.0 / 3)) * args[0].diff()
    }

}

val lg = object : RadicalsLogarithms {
    override val name = "lg"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Десятичный логарифм"

    override fun calculate(args: Array<Double>) = Math.log10(args[0])

    override fun diff(args: Array<Argument>): Argument {
        return args[0].diff() / (args[0] * ln.func(Number(10.0)))
    }

}

val ln = object : RadicalsLogarithms {
    override val name = "ln"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Натуральный логарифм"

    override fun calculate(args: Array<Double>) = Math.log(args[0])

    override fun diff(args: Array<Argument>) = args[0].diff() / args[0]
}

val log = object : RadicalsLogarithms {
    override val name = "log"
    override val argsCount = 2
    override val priority = 0
    override val argsString = "(base, x)"

    override val description: String
        get() = "Логарифм по заданной базе"

    override fun calculate(args: Array<Double>) = Math.log(args[1]) / Math.log(args[0])

    override fun diff(args: Array<Argument>): Argument {
        return (ln.func(args[1]) / ln.func(args[0])).diff()
    }

}

val sqrt = object : RadicalsLogarithms {
    override val name = "√"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Квадратный корень"

    override fun calculate(args: Array<Double>) = Math.sqrt(args[0])

    override fun diff(args: Array<Argument>): Argument {
        return Number(0.5) * (args[0] pow Number(-0.5)) * args[0].diff()
    }

}


/**
 * Тригонометрия
 */


val acos = object : Trigonometry {
    override val name = "acos"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Арккосинус"

    override fun calculate(args: Array<Double>) = Math.acos(args[0])

    override fun diff(args: Array<Argument>) = -power.func(sqrt.func((Number(1.0) - (args[0] pow Number(2.0)))), Number(-1.0)) * args[0].diff()
}

val acot = object : Trigonometry {
    override val name = "acot"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Арккотангенс"

    override fun calculate(args: Array<Double>) = Math.atan(1 / args[0])

    override fun diff(args: Array<Argument>) = -power.func((Number(1.0) + (args[0] pow Number(2.0))), Number(-1.0)) * args[0].diff()

}

val asin = object : Trigonometry {
    override val name = "asin"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Арксинус"

    override fun calculate(args: Array<Double>) = Math.asin(args[0])

    override fun diff(args: Array<Argument>) = power.func(sqrt.func((Number(1.0) - (args[0] pow Number(2.0)))), Number(-1.0)) * args[0].diff()

}

val atan = object : Trigonometry {
    override val name = "atan"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Арктангенс"

    override fun calculate(args: Array<Double>) = Math.atan(args[0])

    override fun diff(args: Array<Argument>) = power.func((Number(1.0) + (args[0] pow Number(2.0))), Number(-1.0)) * args[0].diff()
}

val cos: Trigonometry = object : Trigonometry {
    override val name = "cos"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Косинус"

    override fun calculate(args: Array<Double>) = Math.cos(args[0])

    override fun diff(args: Array<Argument>) = -sin.func(args[0]) * args[0].diff()
}

val cot = object : Trigonometry {
    override val name = "cot"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Котангенс"

    override fun calculate(args: Array<Double>) = 1 / Math.tan(args[0])

    override fun diff(args: Array<Argument>): Argument {
        return -power.func(sin.func(args[0]), Number(-2.0)) * args[0].diff()
    }

}

val sin = object : Trigonometry {
    override val name = "sin"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Синус"

    override fun diff(args: Array<Argument>) = cos.func(args[0]) * args[0].diff()

    override fun calculate(args: Array<Double>) = Math.sin(args[0])
}

val tan = object : Trigonometry {
    override val name = "tan"
    override val argsCount = 1
    override val priority = 0

    override val description: String
        get() = "Тангенс"

    override fun diff(args: Array<Argument>) = (cos.func(args[0]) pow Number(-2)) * args[0].diff()

    override fun calculate(args: Array<Double>) = Math.tan(args[0])
}


/**
 * Без категории
 */


val product = object : MultiOperator {

    override val priority = 9

    override fun calculate(args: Array<Double>): Double {
        var product = 1.0
        for (arg in args)
            product *= arg
        return product
    }

    override fun calculate(args: Array<BigDecimal>): BigDecimal {
        var product = Number.one()
        for (arg in args)
            product *= arg
        return product
    }

    override fun diff(args: Array<Argument>): Argument {
        val args1 = ArrayList<Argument>()
        for (i in 0 until args.size) {
            val args0 = ArrayList<Argument>()
            for (j in 0 until args.size) {
                if (i == j) {
                    args0.add(args[i].diff())
                } else {
                    args0.add(args[j])
                }
            }
            args1.add(Func(args0, this))
        }
        return Func(args1, summa)
    }
}

val summa: MultiOperator = object : MultiOperator {

    override val priority = 10

    override fun calculate(args: Array<Double>): Double {
        var sum = 0.0
        for (arg in args)
            sum += arg
        return sum
    }

    override fun calculate(args: Array<BigDecimal>): BigDecimal {
        var sum = Number.zero()
        for (arg in args)
            sum += arg
        return sum
    }

    override fun diff(args: Array<Argument>): Argument {
        var sum = args[0].diff()
        for (i in 1 until args.size)
            sum += args[i].diff()
        return sum
    }


}

val unaryMinus: Function = object : Function {

    override val section: Int
        get() = FunctionSections.NONE

    override val name = "inv"
    override val argsCount = 1
    override val priority = 0

    override fun diff(args: Array<Argument>) = -args[0].diff()

    override fun calculate(args: Array<Double>) = -args[0]
}
