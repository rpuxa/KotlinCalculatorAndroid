package ru.rpuxa.kotlincalculatorandroid.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.settings.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.activities.settings.NumberFormatActivity
import ru.rpuxa.kotlincalculatorandroid.activities.settings.SettingsGraphActivity
import ru.rpuxa.kotlincalculatorandroid.cache.saveAll
import ru.rpuxa.kotlincalculatorandroid.overrideBackButton
import ru.rpuxa.kotlincalculatorandroid.showBackButton

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        showBackButton(settings_toolbar)

        number_format.setOnClickListener {
            open(NumberFormatActivity::class.java)
        }
        settings_graphics.setOnClickListener {
            open(SettingsGraphActivity::class.java)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        overrideBackButton(item)
        return super.onOptionsItemSelected(item)
    }

    private fun open(clazz: Class<out Activity>) {
        startActivity(Intent(this, clazz))
    }

    override fun onPause() {
        super.onPause()
        saveAll(filesDir)
    }

    override fun onDestroy() {
        super.onDestroy()
        saveAll(filesDir)
    }
}