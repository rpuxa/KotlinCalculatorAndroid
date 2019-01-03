package ru.rpuxa.kotlincalculatorandroid.activities.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.SeekBar
import kotlinx.android.synthetic.main.settings_graphics.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.Settings
import ru.rpuxa.kotlincalculatorandroid.cache.saveAll
import ru.rpuxa.kotlincalculatorandroid.overrideBackButton
import ru.rpuxa.kotlincalculatorandroid.showBackButton

class SettingsGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_graphics)

        showBackButton(settings_graphics_toolbar)

        val line = lineView
        var red = (Settings.Graphics.color ushr 16) and 0xFF
        var green = (Settings.Graphics.color ushr 8) and 0xFF
        var blue = Settings.Graphics.color and 0xFF
        val change = {
            line.changeColor((red shl 16) or (green shl 8) or blue)
        }

        change()
        line_width.progress = Settings.Graphics.lineWidth.toInt()
        line_color_r.progress = red
        line_color_g.progress = green
        line_color_b.progress = blue

        line_width.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                line.changeWidth(progress.toFloat() + 1f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        line_color_r.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                red = progress
                change()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        line_color_g.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                green = progress
                change()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        line_color_b.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                blue = progress
                change()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
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