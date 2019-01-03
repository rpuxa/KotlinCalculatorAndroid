package ru.rpuxa.kotlincalculatorandroid.cache

import ru.rpuxa.kotlincalculatorandroid.SerializableSettings
import ru.rpuxa.kotlincalculatorandroid.settings
import java.io.*

const val SETTINGS = "settings"

fun saveAll(file: File) {
    save(settings.serializable(), file, SETTINGS)
}

fun loadAll(file: File) {
    val s = SuperDeserializator.deserialize(file, SETTINGS)
    settings = if (s != null) s as SerializableSettings else SerializableSettings()
}

fun save(serializable: Any, file: File, name: String) {
    try {
        ObjectOutputStream(FileOutputStream(File(file, name))).use { out ->
            out.writeObject(serializable)
            out.flush()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun load(file: File, name: String): Any? {
    try {
        ObjectInputStream(FileInputStream(File(file, name))).use { out ->
            return out.readObject()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return null
}