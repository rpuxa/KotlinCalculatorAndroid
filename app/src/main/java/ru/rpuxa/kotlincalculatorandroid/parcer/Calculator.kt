package ru.rpuxa.kotlincalculatorandroid.parcer

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument

object Calculator {

    fun optimize(expression: String): Argument? {
        return try {
            Expression.parse(expression).optimize().postOptimize()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }

    }
}
