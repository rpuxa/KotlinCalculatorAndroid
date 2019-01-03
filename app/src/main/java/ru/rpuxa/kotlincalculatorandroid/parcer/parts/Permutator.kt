package ru.rpuxa.kotlincalculatorandroid.parcer.parts

class Permutator<T>(val permutation: Array<T>, private val forEach: (Array<T>) -> Boolean) {

    var broken = false

    init {
        generate(0)
    }

    private fun generate(cur: Int) {
        if (cur == permutation.size) {
            broken = forEach(permutation)
        } else {
            for (index in cur until permutation.size) {
                if (broken)
                    return
                swap(cur, index)
                generate(cur + 1)
                swap(cur, index)
            }
        }
    }

    private fun swap(i: Int, j: Int) {
        val tmp = permutation[i]
        permutation[i] = permutation[j]
        permutation[j] = tmp
    }

}