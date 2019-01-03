package ru.rpuxa.kotlincalculatorandroid

import ru.rpuxa.kotlincalculatorandroid.cache.SuperSerializable

object Settings {
    object FormatNumber {
        var signs
            get() = settings.signs
            set(value) {
                settings.signs = value
            }
        var divider
            get() = settings.divider
            set(value) {
                settings.divider = value
            }
        var isEngineering
            get() = settings.isEngineering
            set(value) {
                settings.isEngineering = value
            }
        var rationalMode = false
    }

    object Graphics {
        var lineWidth
        get() = settings.lineWidth
        set(value) {
            settings.lineWidth = value
        }
        var color
        get() = settings.lineColor
        set(value) {
            settings.lineColor = value
        }
    }
}

class SerializableSettings : SuperSerializable {
    var signs = 10
    var divider = ' '
    var isEngineering = true

    var lineWidth = 2f
    var lineColor = 0xFFFFFF
}


lateinit var settings: SerializableSettings
