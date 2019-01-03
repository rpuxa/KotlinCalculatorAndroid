package ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step0.Number
import java.math.BigDecimal

class MyPair(var key: Argument, var value: BigDecimal) : Comparable<MyPair> {
    override fun compareTo(other: MyPair): Int {
        return when {
            other.key is Number && key is Number -> (key as Number).value.compareTo((other.key as Number).value)
            other.key is Number -> 1
            key is Number -> -1
            else -> value.compareTo(other.value)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MyPair)
            return false

        return key == other.key
    }

    operator fun component1() = key
    operator fun component2() = value
}

class MySet : ArrayList<MyPair>() {

    override fun add(element: MyPair): Boolean {
        for (e in this)
            if (e == element) {
                element.value += e.value
            }
        remove(element)
        return super.add(element)
    }

    fun get(key: Argument): BigDecimal? {
        for (p in this)
            if (p.key == key)
                return p.value
        return null
    }
}