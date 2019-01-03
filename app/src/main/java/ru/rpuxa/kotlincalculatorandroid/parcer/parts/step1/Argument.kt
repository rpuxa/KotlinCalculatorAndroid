package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1

import ru.rpuxa.kotlincalculatorandroid.parcer.*
import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import java.io.Serializable
import java.math.BigDecimal

abstract class Argument: ExpressionPart, Serializable {

    operator fun div(argument: Argument) = divide.func(this, argument)

    operator fun plus(argument: Argument) = summa.func(this, argument)

    operator fun minus(argument: Argument) = minus.func(this, argument)

    operator fun unaryMinus() = unaryMinus.func(this)

    operator fun times(argument: Argument) = product.func(this, argument)

    infix fun pow(argument: Argument) = power.func(this, argument)

    abstract fun calculate(): BigDecimal

    abstract fun calculateWithVar(names: Array<String>, values: Array<BigDecimal>): BigDecimal

    abstract fun calculateWithVar(name: String, value: Float): Float

    fun calculateWithVar(name: String, value: BigDecimal) = calculateWithVar(arrayOf(name), arrayOf(value))

    open fun optimize() = this

    open fun postOptimize() = this

    abstract fun getPower(): BigDecimal

    abstract fun getBase(): Argument

    abstract fun diff(): Argument

    abstract fun getMultiplierSign(): Boolean

    abstract fun removeMinus()

    abstract fun copy(): Argument

    fun isFunc(func: Function) = this is Func && this.impl.name == func.name
}