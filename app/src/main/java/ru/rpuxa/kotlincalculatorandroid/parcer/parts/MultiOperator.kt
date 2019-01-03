package ru.rpuxa.kotlincalculatorandroid.parcer.parts

import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import ru.rpuxa.kotlincalculatorandroid.parcer.FunctionSections

interface MultiOperator : Function {

    override val argsCount
            get() = 0

    override val name
            get() = "___$$$\$null\$$$\$___"

    override val section: Int
        get() = FunctionSections.NONE
}