package ru.rpuxa.kotlincalculatorandroid

object History {
    private val items = ArrayList<Item>()
    private var index = -1

    fun add(text: String, cursorPosition: Int) {
        index++
        if (items.isNotEmpty())
            for (i in items.size - 1 downTo index)
                items.removeAt(i)
        items.add(Item(text, cursorPosition))
    }

    fun undo(): Item? {
        index--
        if (index < -1) {
            index = -1
            return null
        }

        if (index == -1)
            return Item()

        return items[index]
    }

    fun redo(): Item? {
        index++
        if (index >= items.size) {
            index--
            return null
        }

        return items[index]
    }

    data class Item(val text: String = "", val cursorPosition: Int = 0)
}