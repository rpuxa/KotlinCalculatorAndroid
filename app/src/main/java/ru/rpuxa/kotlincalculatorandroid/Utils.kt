package ru.rpuxa.kotlincalculatorandroid

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import java.math.BigDecimal
import java.math.BigInteger

fun AppCompatActivity.showBackButton(toolbar: Toolbar) {
    setSupportActionBar(toolbar)

    if (supportActionBar != null) {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }
}

fun AppCompatActivity.overrideBackButton(item: MenuItem?) {
    if (item!!.itemId == android.R.id.home)
        finish()
}

fun BigDecimal.gcd(other: BigDecimal): BigDecimal? {
    if (!isInt() || !other.isInt())
        return null
    var a = toBigInteger()
    var b = other.toBigInteger()
    while (a.compareTo(BigInteger.ZERO) != 0) {
        val tmp = a
        a = b % a
        b = tmp
    }
    return BigDecimal(b)
}

fun BigDecimal.isOne() = compareTo(BigDecimal.ONE) == 0

fun BigDecimal.isZero() = compareTo(BigDecimal.ZERO) == 0

fun BigDecimal.isMinusOne() = compareTo(BigDecimal.ONE.negate()) == 0

fun BigDecimal.isMoreThenOne() = compareTo(BigDecimal.ONE) == 1

fun BigDecimal.isMoreThenZero() = compareTo(BigDecimal.ZERO) == 1

fun BigDecimal.isInt(): Boolean {
    val stripTrailingZeros = stripTrailingZeros()
    val scale = stripTrailingZeros.scale()
    return scale <= 0
}