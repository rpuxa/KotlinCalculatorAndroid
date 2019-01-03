package ru.rpuxa.kotlincalculatorandroid.parcer

interface Operator : Function {
    override val argsCount
    get() = 2

    override val section: Int
        get() = FunctionSections.NONE
}