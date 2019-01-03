package ru.rpuxa.kotlincalculatorandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.graph_activity.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.activities.settings.SettingsGraphActivity
import ru.rpuxa.kotlincalculatorandroid.graph.GRAPH_BUNDLE
import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument

class GraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val func = intent.extras.getSerializable(GRAPH_BUNDLE) as Argument
        val diff = func.diff().optimize()
        setContentView(R.layout.graph_activity)
        graph2d.addGraph(
                { arg: Float -> func.calculateWithVar("x", arg) },
                { arg: Float -> diff.calculateWithVar("x", arg) }
        )

        graph_settings.setOnClickListener {
            startActivity(Intent(this, SettingsGraphActivity::class.java))
        }

    }

    override fun onDestroy() {
        graph2d.removeAllGraph()
        super.onDestroy()
    }
}