package ru.rpuxa.kotlincalculatorandroid.parcer.types

import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import ru.rpuxa.kotlincalculatorandroid.parcer.FunctionSections

interface OtherFunction : Function {
    override val section: Int
        get() = FunctionSections.OTHER
}