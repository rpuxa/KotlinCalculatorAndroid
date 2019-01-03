package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0

import ru.rpuxa.kotlincalculatorandroid.*
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.fullOptimize
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


class Number(var num: BigDecimal, var den: BigDecimal = Number.one()) : Argument() {

    constructor(value: Int) : this(BigDecimal(value).setScale(16, RoundingMode.HALF_EVEN))

    constructor(value: Double) : this(BigDecimal(value).setScale(16, RoundingMode.HALF_EVEN))

    init {
        num.setScale(16, RoundingMode.HALF_EVEN)
        den.setScale(16, RoundingMode.HALF_EVEN)
    }

    val value
        get() = num / den

    override fun calculateWithVar(name: String, value: Float) = this.value.toFloat()

    override fun optimize(): Argument {
        if (den.isOne())
            return this
        if (!den.isMoreThenZero()) {
            return Number(num.negate(), den.abs())
        }
        else if (fullOptimize)
            return Number(num / den)
        val gcd = (num.gcd(den) ?: return this).abs()

        return Number(num / gcd, den / gcd)
    }

    override fun getBase() = this

    override fun getPower() = Number.one()

    override fun diff() = Number(0.0)

    override fun calculate() = value

    fun isRational() = num.stripTrailingZeros().scale() <= 0 && den.stripTrailingZeros().scale() <= 0

    override fun calculateWithVar(names: Array<String>, values: Array<BigDecimal>) = calculate()

    operator fun plus(other: Number): Number {
        return if (isRational() && other.isRational())
            Number(num * other.den + den * other.num, den * other.den)
        else
            Number(value + other.value)
    }

    operator fun times(other: Number): Number {
        return Number(other.num * num, den * other.den)
    }

    fun pow(int: Int): Number {
        return Number(num.pow(int), den.pow(int))
    }

    fun isZero(): Boolean {
        return value.isZero()
    }

    fun isMoreThenZero(): Boolean {
        val value1 = value
        val b = value1.compareTo(BigDecimal.ZERO) == 1
        return b
    }
    fun isInt() = value.stripTrailingZeros().scale() <= 0

    fun negate() {
        num = num.negate()
    }

    fun isOne() = value.isOne()

    fun isMinusOne() = value.isMinusOne()

    fun isMoreThenOne() = value.isMoreThenOne()

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Number)
            return false
        return other.num.compareTo(num) == 0 && other.den.compareTo(den) == 0
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        val n = optimize() as Number
        val s = doubleToString(n.num)
        if (n.den.isOne())
            return s
        return "$s / ${n.den.toBigInteger()}"
    }

    override fun getMultiplierSign() = num.signum() > 0 && den.signum() > 0

    override fun removeMinus() {
        num = num.abs()
        den = den.abs()
    }

    override fun copy() = Number(num, den)

    fun divide(other: Number) =
            if (other.isRational() && isRational())
                Number(num * other.den, den * other.num)
            else
                Number(value / other.value)

    companion object {

        private val veryVeryMany = BigDecimal("999999999999999999999999999999999999999999999999999999999999999999999999999999999")

        fun one(): BigDecimal {
            return BigDecimal.ONE.setScale(16, RoundingMode.HALF_EVEN)
        }

        fun parse(string: String): Number? {
            return try {
                for (c in string.toCharArray())
                    if (c == '+')
                        return null

                Number(BigDecimal(string).setScale(16, RoundingMode.HALF_EVEN))
            } catch (e: NumberFormatException) {
                null
            }
        }

        fun doubleToString(value: BigDecimal): String {
            if (value.compareTo(veryVeryMany) == 1)
                return "∞"
            if (value.abs().compareTo(veryVeryMany) == 1)
                return "-∞"
            val bd = value.setScale(Settings.FormatNumber.signs, RoundingMode.HALF_EVEN)

            val fractional = bd.rem(BigDecimal.ONE).abs()
            val int = bd.toBigInteger()
            /*    if (Settings.FormatNumber.isEngineering) {
                    return bd.toString()
                }*/
            if (int.compareTo(BigInteger.ZERO) == 0 && fractional.isZero())
                return "0"
            if (int.compareTo(BigInteger.ZERO) == 0) {
                return fractional.toDouble().toString()
            }
            var integerToString = int.toString()
            if (Settings.FormatNumber.divider != Character.MIN_VALUE) {
                var index = integerToString.length % 3
                if (index == 0)
                    index = 3
                while (index < integerToString.length) {
                    integerToString = integerToString.substring(0, index) + Settings.FormatNumber.divider + integerToString.substring(index)
                    index += 4
                }
            }
            return if (fractional.isZero())
                integerToString
            else
                integerToString + fractional.toDouble().toString().substring(1)
        }

        fun zero(): BigDecimal {
            return BigDecimal.ZERO.setScale(16, RoundingMode.HALF_EVEN)
        }

        val ONE = Number(1)
        val ZERO = Number(0)
        val MINUS_ONE = Number(-1)
    }
}
