package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1

import ru.rpuxa.kotlincalculatorandroid.parcer.ParseException
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import java.math.BigDecimal

open class Variable(val name: String) : Argument() {

    override fun calculateWithVar(name: String, value: Float): Float {
        if (name == this.name)
            return value
        throw ParseException()
    }

    override fun getBase() = this

    override fun getPower() = Number.one()

    override fun calculate(): BigDecimal {
        throw ParseException()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Variable)
            return false
        return other.name == name
    }

    override fun getMultiplierSign() = true

    override fun removeMinus() {
    }

    override fun copy() = Variable(name)

    override fun diff() =
            if (name == "x")
                Number(1.0)
            else
                this

    override fun calculateWithVar(names: Array<String>, values: Array<BigDecimal>): BigDecimal {
        for (i in 0 until names.size) {
            if (names[i] == name)
                return values[i]
        }
        return calculate()
    }

    override fun toString() = name

    companion object {
        val variables = arrayOf(
                Variable("x"),
                Variable("y"),
                Variable("z")
        )

        fun parse(string: String): Variable? {
            for (v in variables)
                if (v.name == string)
                    return v
            return null
        }
    }
}