package ru.rpuxa.kotlincalculatorandroid.parcer.types

import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import ru.rpuxa.kotlincalculatorandroid.parcer.FunctionSections

interface Hyperbolic : Function {
    override val section: Int
        get() = FunctionSections.HYPERBOLIC
    override val argsCount: Int
        get() = 1

}