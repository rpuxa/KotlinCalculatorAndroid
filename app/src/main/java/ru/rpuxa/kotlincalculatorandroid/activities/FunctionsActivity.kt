package ru.rpuxa.kotlincalculatorandroid.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.functions_activity.*
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.lists.func.FunctionPageAdapter

class FunctionsActivity : AppCompatActivity() {

    val print: (String) -> Unit = {
        val i = Intent()
        setResult(Activity.RESULT_OK, i)
        i.putExtra(PRINT_FUNC, it)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.functions_activity)
        functions_pager.adapter = FunctionPageAdapter(supportFragmentManager)
    }
}