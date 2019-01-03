package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import java.math.BigDecimal

class Constant(name: String, val value: BigDecimal, val description: String, val sections: Int) : Variable(name) {

    constructor(name: String, value: Double, description: String, sections: Int) : this(name, BigDecimal(value), description, sections)

    override fun calculateWithVar(name: String, value: Float) = this.value.toFloat()

    override fun calculate() = value

    override fun calculateWithVar(names: Array<String>, values: Array<BigDecimal>) = value

    override fun diff() = Number(0.0)

    override fun getMultiplierSign() = true

    override fun optimize() = if (fullOptimize) Number(value) else this

    companion object {
        val constants = arrayOf(
                Constant("e", Math.E, "Число Эйлера", ConstantSections.MATHEMATICAL),
                Constant("π", Math.PI, "Число Пи", ConstantSections.MATHEMATICAL),
                Constant("γ", 0.577_215_664_901_532_860_606_512, "Постоянная Эйлера — Маскерони", ConstantSections.MATHEMATICAL),
                Constant("c", 299_792_458.0, "Скорость света в вакууме (м/с)", ConstantSections.PHYSICAL)
        )

        fun isConst(text: String): Boolean {
            for (c in constants)
                if (c.name == text)
                    return true

            return false
        }

        fun parse(text: String): Constant? {
            for (c in constants)
                if (c.name == text)
                    return c

            return null
        }
    }
}

object ConstantSections {
    const val NONE = -1
    const val MATHEMATICAL = 0
    const val PHYSICAL = 1

    val NAMES = arrayOf(
            "Математические постоянные",
            "Физические постоянные"
    )
}