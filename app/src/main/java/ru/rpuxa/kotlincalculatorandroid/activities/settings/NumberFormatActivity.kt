package ru.rpuxa.kotlincalculatorandroid.activities.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.SeekBar
import kotlinx.android.synthetic.main.settings_format_number.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.Settings
import ru.rpuxa.kotlincalculatorandroid.cache.saveAll
import ru.rpuxa.kotlincalculatorandroid.overrideBackButton
import ru.rpuxa.kotlincalculatorandroid.showBackButton

class NumberFormatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_format_number)

        showBackButton(format_number_toolbar)

        when (Settings.FormatNumber.divider) {
            ' ' -> space.isChecked = true
            '\'' -> apostroph.isChecked = true
            '_' -> underscore.isChecked = true
            Character.MIN_VALUE -> no_divider.isChecked = true
        }

        signs_bar.progress = Settings.FormatNumber.signs - 2

        signs_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    Settings.FormatNumber.signs = progress + 2
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        space.setOnClickListener {
            Settings.FormatNumber.divider = ' '
        }

        apostroph.setOnClickListener {
            Settings.FormatNumber.divider = '\''
        }

        underscore.setOnClickListener {
            Settings.FormatNumber.divider = '_'
        }

        no_divider.setOnClickListener {
            Settings.FormatNumber.divider = Character.MIN_VALUE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        overrideBackButton(item)
        return super.onOptionsItemSelected(item)
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