package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1

import ru.rpuxa.kotlincalculatorandroid.isInt
import ru.rpuxa.kotlincalculatorandroid.isMoreThenZero
import ru.rpuxa.kotlincalculatorandroid.isOne
import ru.rpuxa.kotlincalculatorandroid.parcer.*
import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.MultiOperator
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import java.math.BigDecimal

typealias Args = ArrayList<Argument>

var fullOptimize = true

class Func(val args: Args, val impl: Function) : Argument() {

    override fun calculate(): BigDecimal {
        val arr = Array<BigDecimal>(args.size) { BigDecimal.ZERO }
        for ((i, arg) in args.withIndex()) {
            arr[i] = arg.calculate()
        }
        return impl.calculate(arr)
    }

    override fun diff() = impl.diff(args.toTypedArray())

    override fun calculateWithVar(names: Array<String>, values: Array<BigDecimal>): BigDecimal {
        val arr = Array<BigDecimal>(args.size) { BigDecimal.ZERO }
        for ((i, arg) in args.withIndex()) {
            arr[i] = arg.calculateWithVar(names, values)
        }
        return impl.calculate(arr)
    }

    override fun calculateWithVar(name: String, value: Float): Float {
        val arr = Array(args.size) { 0.0 }
        for ((i, arg) in args.withIndex()) {
            arr[i] = arg.calculateWithVar(name, value).toDouble()
        }
        return impl.calculate(arr).toFloat()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Func)
            return false
        if (other.args.size != args.size)
            return false
        if (impl == product || impl ==summa) {
            label@ for (arg in args) {
                for (arg1 in other.args) {
                    if (arg1 == arg)
                        continue@label
                }
                return false
            }
        } else
            for (i in args.indices)
                if (args[i] != other.args[i])
                    return false

        return impl.name == other.impl.name
    }

    private fun superEquals(other: Func): Boolean {
        if (other.args.size != args.size)
            return false
        for (i in args.indices)
            if (args[i] != other.args[i])
                return false

        return impl.name == other.impl.name
    }

    override fun postOptimize(): Argument {
        val args = Args()
        for (arg in this.args) {
            val a = arg.postOptimize()
            args.add(a)
        }
        when (impl) {
            product -> {
                val multiply = Args()
                val divide = Args()

                for (arg in args) {
                    if (arg is Func && arg.impl == power && arg.args[1] is Number && (arg.args[1] as Number).isInt() && !(arg.args[1] as Number).isMoreThenZero()) {
                        val argument = arg.args[1] as Number
                        argument.negate()
                        if (argument.isOne()) {
                            divide.add(arg.args[0])
                        } else
                            divide.add(arg.args[0] pow argument)
                    } else if (arg is Func && arg.impl == divide) {
                        val argument = arg.args[0] as Number
                        if (!argument.isOne()) {
                            multiply.add(argument)
                        }
                        divide.add(arg.args[1])
                    } else if (arg is Number) {
                        if (!arg.num.isOne())
                            multiply.add(Number(arg.num))
                        if (!arg.den.isOne())
                            divide.add(Number(arg.den))
                    } else
                        multiply.add(arg)
                }

                if (divide.size == 1)
                    return (Func(multiply, product) / divide[0]).postOptimize()
                else if (divide.size > 1)
                    return (Func(multiply, product) / Func(divide, product)).postOptimize()
            }

            power -> {
                val arg = args[1]
                if (arg is Number && arg.isMinusOne()) {
                    return (Number(1.0) / args[0]).postOptimize()
                }
            }
        }

        if (fullOptimize) {
            for (arg in args)
                if (arg !is Number)
                    return Func(args, impl)
        } else
            return Func(args, impl)

        return Number(calculate())
    }

    override fun optimize(): Argument {
        val args = Args()
        for (arg in this.args) {
            val a = arg.optimize()
            args.add(a)
        }

        when (impl) {

            definiteIntegral -> return Number(Integrals.definiteIntegral(args) ?: return this)

            diff -> return args[0].diff().optimize()

            plus -> return summa.func(args[0], args[1]).optimize()

            minus -> return summa.func(args[0], -args[1]).optimize()

            multiply -> return product.func(args[0], args[1]).optimize()

            divide -> {
                val arg0 = args[0]
                val arg1 = args[1]
                return if (arg1 is Number && arg0 is Number) {
                    val divide = arg0.divide(arg1)
                    divide.optimize()
                } else if (arg1 is Number && arg1.isInt())
                    product.func(args[0], Number(Number.one(), arg1.value)).optimize()
                else
                    product.func(args[0], arg1 pow Number(-1)).optimize()
            }

            is MultiOperator -> {
                when (impl) {
                    product -> {
                        val args0 = Args(args)
                        label@ while (true) {
                            for (i in 0 until args0.size) {
                                val arg = args0[i]
                                if (arg is Func && (arg.impl == multiply || arg.impl == product)) {
                                    args0.removeAt(i)
                                    args0.addAll(arg.args)
                                    continue@label
                                } else if (arg is Func && arg.impl == divide) {
                                    args0.removeAt(i)
                                    args0.add(arg.args[0])
                                    args0.add(arg.args[1] pow Number(-1))
                                    continue@label
                                }
                            }
                            break
                        }
                        if (args.size != args0.size)
                            return Func(args0, product).optimize()

                        for (arg in args)
                            if (arg is Number && arg.isZero())
                                return Number(0.0)
                        var i = 0
                        while (i < args.size && args.size > 1) {
                            val num = args[i]
                            if (num is Number && num.isOne()) {
                                args.removeAt(i)
                            } else
                                i++
                        }

                        val opened = openBrackets(args)
                        if (opened != null)
                            return opened.optimize()

                        val monomial = getMonomial(this)

                        val args2 = Args()
                        if (monomial.multiplier != Number.ONE || monomial.map.isEmpty())
                            args2.add(monomial.multiplier)
                        for ((key, value) in monomial.map) {
                            if (value.isOne())
                                args2.add(key)
                            else
                                args2.add(key pow Number(value))
                        }

                        val optimize = Func(args2, product)
                        if (!superEquals(optimize))
                            return optimize.optimize()
                    }

                    summa -> {
                        val args0 = Args(args)
                        label@ while (true) {
                            for (i in 0 until args0.size) {
                                val arg = args0[i]
                                if (arg is Func && (arg.impl == plus || arg.impl == summa)) {
                                    args0.removeAt(i)
                                    args0.addAll(arg.args)
                                    continue@label
                                } else if (arg is Func && arg.impl == minus) {
                                    args0.removeAt(i)
                                    args0.add(arg.args[0])
                                    args0.add(-arg.args[1])
                                    continue@label
                                }
                            }
                            break
                        }
                        if (args.size != args0.size)
                            return Func(args0, summa).optimize()

                        var i = 0
                        while (i < args.size && args.size > 1) {
                            val num = args[i]
                            if (num is Number && num.isZero()) {
                                args.removeAt(i)
                            } else
                                i++
                        }

                        val monomials = ArrayList<Monomial>()

                        for (arg in args) {
                            val monomial = getMonomial(arg)
                            for ((k, m) in monomials.withIndex())
                                if (m.equals(monomial)) {
                                    monomials.removeAt(k)
                                    monomial.multiplier += m.multiplier
                                    break
                                }
                            monomials.add(monomial)
                        }

                        val args1 = Args()
                        for (m in monomials) {
                            val args2 = Args()
                            if (m.multiplier != Number.ONE || m.map.isEmpty())
                                args2.add(m.multiplier)
                            for ((key, value) in m.map) {
                                if (value.isOne())
                                    args2.add(key)
                                else
                                    args2.add(key pow Number(value))
                            }
                            args1.add(Func(args2, product))
                        }

                        val optimize = Func(args1, summa)
                        if (optimize != this)
                            return optimize.optimize()
                    }

                }


                if (args.size == 1)
                    return args[0].optimize()
            }

            unaryMinus -> {
                return product.func(Number(-1), args[0]).optimize()
            }

            power -> {
                val firstArg = args[0]
                val secondArg = args[1]
                if (firstArg is Number && (firstArg.isOne() || firstArg.isZero())) {
                    return Number(firstArg.value)
                }
                if (secondArg is Number) {
                    if (secondArg.isZero())
                        return Number(1.0)
                    if (secondArg.isOne())
                        return firstArg
                }

                if (secondArg is Number && firstArg is Func && firstArg.impl == summa && secondArg.isMoreThenOne() && secondArg.isInt()) {
                    val args1 = Args()
                    repeat(secondArg.value.toInt()) {
                        args1.add(firstArg)
                    }
                    val opened = openBrackets(args1)
                    if (opened != null)
                        return opened.optimize()
                }

                if (secondArg is Number && firstArg is Number && secondArg.isMoreThenZero() && firstArg.isInt() && secondArg.isInt()) {
                    return Number(power.calculate(arrayOf(firstArg.value, secondArg.value)))
                }
            }
        }

        if (fullOptimize) {
            for (arg in args)
                if (arg !is Number)
                    return Func(args, impl)
        } else
            return Func(args, impl)

        return Number(calculate())
    }

    private fun openBrackets(args: Args): Func? {
        val argsSum = Args()
        var m = 1

        for (arg in args)
            if (arg is Func && arg.impl == summa)
                m *= arg.args.size

        if (m == 1)
            return null

        val getCount = { n: Int ->
            var s = 0
            for (i in n + 1 until args.size) {
                val argument = args[i]
                when (argument) {
                    is Variable, is Number -> s++
                    is Func -> if (argument.impl == summa)
                        s += argument.args.size
                    else
                        s++
                }
            }
            s
        }

        val getArg = { n: Int, i: Int ->
            val argument = args[n]
            when (argument) {
                is Variable, is Number -> argument
                is Func -> if (argument.impl == summa) {
                    val count = getCount(n)
                    if (count != 0)
                        argument.args[(i / count) % argument.args.size]
                    else
                        argument.args[i % argument.args.size]
                } else
                    argument
                else -> null
            }
        }

        for (i in 0 until m) {
            val args0 = Args()
            for (n in args.indices) {
                val element = getArg(n, i)!!
                args0.add(element)
            }
            argsSum.add(Func(args0, product))
        }

        return Func(argsSum, summa)
    }

    private fun getMonomial(argument: Argument): Monomial {
        val monomial = Monomial()
        when (argument) {
            is Variable -> monomial.map.add(MyPair(argument, Number.one()))
            is Number -> {
                monomial.multiplier = argument
                return monomial
            }
            is Func -> {
                when (argument.impl) {
                    power -> {
                        val secondArg = argument.args[1]
                        //  val firstArg = argument.args[0]
                        if (secondArg is Number) {
                            monomial.map.add(MyPair(argument.args[0], secondArg.value))
                        } else
                            monomial.map.add(MyPair(argument, Number.one()))
                    }
                    product -> {
                        for (arg in argument.args)
                            when (arg) {
                                is Variable -> monomial.map.add(MyPair(arg, Number.one()))
                                is Number -> {
                                    if (fullOptimize)
                                        monomial.multiplier *= arg
                                    else
                                        monomial.map.add(MyPair(arg, Number.one()))
                                }
                                is Func -> {
                                    when (arg.impl) {
                                        power ->
                                            if (arg.args[1] is Number)
                                                monomial.map.add(MyPair(arg.args[0], (arg.args[1] as Number).value))
                                            else
                                                monomial.map.add(MyPair(arg, Number.one()))
                                        else -> monomial.map.add(MyPair(arg, Number.one()))
                                    }
                                }
                            }
                    }

                    else -> monomial.map.add(MyPair(argument, Number.one()))
                }
            }
        }

        if (fullOptimize)
            return monomial

        var sizeBest = 10000000
        var bestArgs = MySet()
        var mBest = Number(-1)

        label@ for (n in 1..(1 shl monomial.map.size)) {
            var m = Number(1)
            val args = MySet()
            for (i in monomial.map.indices) {
                val key = monomial.map[i].key
                if ((n ushr i) and 1 == 1 && key is Number && monomial.map[i].value.isMoreThenZero() && monomial.map[i].value.isInt()) {
                    m *= key.pow(monomial.map[i].value.toInt())
                } else
                    args.add(monomial.map[i])
                if (!m.isRational())
                    continue@label
            }

            if (args.size < sizeBest) {
                sizeBest = args.size
                bestArgs = args
                mBest = m
            }
        }

        monomial.map = bestArgs
        monomial.multiplier = mBest

        monomial.map.sort()

        return monomial
    }

    override fun toString(): String {
        val func = copy() as Func
        when (func.impl) {
            is Operator -> {
                var ans = ""
                for ((i, arg) in func.args.withIndex()) {
                    ans += if (arg is Func && (((arg.impl is Operator || arg.impl is MultiOperator) && arg.impl.priority > func.impl.priority) ||
                                    (i == 1 && func.impl == divide && func.args[1].isFunc(product))))
                        "($arg)"
                    else
                        arg

                    if (i == 0)
                        ans += " ${func.impl.name} "
                }
                return ans
            }
            is MultiOperator -> {
                val sign = when (func.impl) {
                    summa -> " + "
                    product -> " * "
                    else -> "error"
                }
                var s = ""

                if (!func.getMultiplierSign()) {
                    s = "-"
                    func.removeMinus()
                } else if (!func.args[0].getMultiplierSign()) {
                    func.args[0].removeMinus()
                    s = "-"
                }
                for ((i, arg) in func.args.withIndex()) {
                    s += if (arg is Func && (arg.impl is Operator || arg.impl is MultiOperator) && arg.impl.priority > func.impl.priority)
                        "($arg)"
                    else
                        arg

                    if (i != func.args.size - 1) {
                        s += if (func.impl == summa && !func.args[i + 1].getMultiplierSign()) {
                            func.args[i + 1].removeMinus()
                            " - "
                        } else if (func.impl != product || func.args[i] is Number && func.args[i + 1] is Number || func.args[i].isFunc(power))
                            sign
                        else
                            ""

                    }
                }
                return s
            }

            else -> {
                var s = "${func.impl.name}("
                for ((i, arg) in func.args.withIndex()) {
                    s += "$arg"
                    if (i != func.args.lastIndex)
                        s += ", "
                }
                return "$s)"
            }
        }
    }

    override fun getMultiplierSign(): Boolean {
        if (impl == product) {
            for (arg in args) {
                if (arg is Number)
                    return arg.isMoreThenZero()
            }
            return true
        } else
            return true
    }

    override fun removeMinus() {
        if (impl == product) {
            val arg = args[0]
            if (arg is Number) {
                if (arg.isMinusOne()) {
                    args.remove(arg)
                } else
                    arg.negate()
                return
            }

        }
    }

    override fun copy(): Argument {
        val args0 = Args()
        for (arg in args)
            args0.add(arg.copy())
        return Func(args0, impl)
    }

    override fun getPower() = when (impl) {
        power -> {
            val argument = args[1].optimize()
            if (argument is Number)
                argument.value
            else
                Number.one()
        }
        else -> Number.one()
    }


    override fun getBase() = when (impl) {
        power -> {
            if (args[1].optimize() is Number)
                args[0]
            else
                this
        }
        else -> this
    }

    private class Monomial {

        var multiplier = Number(1)
        var map = MySet()

        fun equals(monomial: Monomial): Boolean {
            if (map.size != monomial.map.size)
                return false
            label@ for ((arg, pow) in map) {
                for ((arg1, pow1) in monomial.map) {
                    if (arg == arg1 && pow == pow1)
                        continue@label
                }
                return false
            }

            return true
        }
    }

}