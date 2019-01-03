package ru.rpuxa.kotlincalculatorandroid.activities


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ru.rpuxa.kotlincalculatorandroid.History
import ru.rpuxa.kotlincalculatorandroid.R
import ru.rpuxa.kotlincalculatorandroid.Settings
import ru.rpuxa.kotlincalculatorandroid.cache.loadAll
import ru.rpuxa.kotlincalculatorandroid.cache.saveAll
import ru.rpuxa.kotlincalculatorandroid.graph.GRAPH_BUNDLE
import ru.rpuxa.kotlincalculatorandroid.parcer.Calculator
import ru.rpuxa.kotlincalculatorandroid.parcer.Function
import ru.rpuxa.kotlincalculatorandroid.parcer.Run
import ru.rpuxa.kotlincalculatorandroid.views.*

const val PRINT_FUNC = "printFunc"
const val PRINT_CONST = "printConst"

class MainActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadAll(filesDir)
        Run.main()

        expression.setRawInputType(InputType.TYPE_CLASS_TEXT)
        expression.setTextIsSelectable(true)
        val font = Typeface.createFromAsset(assets, "comic.ttf")
        expression.typeface = font
        answer.setRawInputType(InputType.TYPE_CLASS_TEXT)
        answer.setTextIsSelectable(true)
        answer.setOnTouchListener { _, _ -> true }
        answer.typeface = font
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val f = data!!.extras.getString(PRINT_FUNC)
            if (f != null)
                printFunc(f)
            val c = data.extras.getString(PRINT_CONST)
            if (c != null)
                print(c)
        }
    }

    override fun onPause() {
        super.onPause()
        saveAll(filesDir)
    }

    override fun onDestroy() {
        super.onDestroy()
        saveAll(filesDir)
    }

    private fun setListeners() {
        val b = ButtonActions()
        val buttons = arrayOf(
                a1, a2, a3, a4, a5,
                b1, b2, b3, b4, b5,
                c1, c2, c3, c4, c5,
                d1, d2, d3, d4, d5,
                e1, e2, e3, e4, e5
        )
        val actions = arrayOf(
                b.a1, b.a2, b.a3, b.a4, b.a5,
                b.b1, b.b2, b.b3, b.b4, b.b5,
                b.c1, b.c2, b.c3, b.c4, b.c5,
                b.d1, b.d2, b.d3, b.d4, b.d5,
                b.e1, b.e2, b.e3, b.e4, b.e5
        )
        for (i in buttons.indices)
            buttons[i].setCalculatorListener(actions[i])
    }

    fun printFunc(text: String) {
        if (expression.selectionStart == 0 && expression.text.isNotEmpty() && expression.text.toString()[0] == '(') {
            var bracket = 0
            for (c in expression.text.toString()) {
                when (c) {
                    '(' -> bracket++
                    ')' -> bracket--
                }
            }

            if (bracket == 0) {
                print(text)
                expression.setSelection(expression.text.toString().lastIndex)
                return
            }
        }
        print("$text()", true)
    }

    fun print(text: String, isFunc: Boolean = false) {
        val string = expression.text.toString()
        val selectionStart = expression.selectionStart
        val s = string.substring(0, expression.selectionStart) + text + string.substring(expression.selectionEnd)
        expression.setText(s)
        if (!isFunc)
            expression.setSelection(selectionStart + text.length)
        else
            expression.setSelection(selectionStart + text.length - 1)
        onChange()
    }

    fun delete() {
        if (expression.selectionEnd == 0)
            return
        val s: String
        val text = expression.text.toString()
        val start = expression.selectionStart
        if (start == expression.selectionEnd) {
            if (text[start - 1] != ')')
                for (begin in 0 until start) {
                    for (func in Function.functions) {
                        val index = text.indexOf(func.name + "(", begin)
                        if (index == -1 || index > start)
                            continue
                        val end = func.name.length + index + 1
                        if (end < start)
                            continue
                        var bracket = 1
                        for (i in end..text.lastIndex) {
                            when (text[i]) {
                                '(' -> bracket++
                                ')' -> bracket--
                            }
                            if (bracket == 0) {
                                expression.setText(text.removeRange(i, i + 1).removeRange(index, end))
                                expression.setSelection(index)
                                onChange()
                                return
                            }
                        }
                        s = text.removeRange(index, end)
                        expression.setText(s)
                        expression.setSelection(index)
                        onChange()
                        return
                    }
                }

            s = text.substring(0, expression.selectionStart - 1) + text.substring(expression.selectionStart)
            expression.setText(s)
            expression.setSelection(start - 1)
            onChange()
            return
        }

        s = text.substring(0, expression.selectionStart) + text.substring(expression.selectionEnd)
        expression.setText(s)
        expression.setSelection(start)
        onChange()
    }

    fun onChange(save: Boolean = true) {
        Thread {
            val text = expression.text.toString()
            val ans = Calculator.optimize(text)
            this@MainActivity.runOnUiThread {
                if (ans == null || ans.toString().isEmpty())
                    answer.setText("")
                else {
                    answer.setText(ans.toString())
                }
                if (save)
                    History.add(text, expression.selectionStart)
            }
        }.start()
    }

    var toast = null as Toast?
    fun showToast(text: String, isShort: Boolean = true) {
        if (toast != null)
            toast!!.cancel()
        toast = Toast.makeText(this, text, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        toast!!.show()
    }

    inner class ButtonActions {
        val a1 = a1@{ action: Int ->
            when (action) {
                MAIN -> {
                    print("7")
                }
                NORTH_WEST_BUTTON -> {
                }
                NORTH_EAST_BUTTON -> {
                    val arg = Calculator.optimize(expression.text.toString()) ?: return@a1
                    val i = Intent(this@MainActivity, GraphActivity::class.java)
                    i.putExtra(GRAPH_BUNDLE, arg)
                    startActivity(i)
                }
                SOUTH_EAST_BUTTON -> {
                }
                SOUTH_WEST_BUTTON -> {
                }
            }
        }

        val a2 = { action: Int ->
            when (action) {
                MAIN -> print("8")
                NORTH_WEST_BUTTON -> print("e")
                SOUTH_WEST_BUTTON -> print("π")
            }
        }

        val a3 = { action: Int ->
            when (action) {
                MAIN -> print("9")
                NORTH_WEST_BUTTON -> print("x")
            }
        }

        val a4 = { action: Int ->
            when (action) {
                MAIN -> print("+")
            }
        }

        val a5 = { action: Int ->
            when (action) {
                MAIN -> delete()
                NORTH_EAST_BUTTON -> {
                    expression.setText("")
                    onChange()
                }
            }
        }

        val b1 = { action: Int ->
            when (action) {
                MAIN -> print("4")
                NORTH_EAST_BUTTON -> printFunc("lg")
                SOUTH_EAST_BUTTON -> printFunc("ln")
                NORTH_WEST_BUTTON -> printFunc("log")
            }
        }

        val b2 = { action: Int ->
            when (action) {
                MAIN -> print("5")
            }
        }

        val b3 = { action: Int ->
            when (action) {
                MAIN -> print("6")
            }
        }

        val b4 = { action: Int ->
            when (action) {
                MAIN -> print("-")
            }
        }

        val b5 = { action: Int ->
            when (action) {
                MAIN -> {
                    val i = Intent(this@MainActivity, FunctionsActivity::class.java)
                    startActivityForResult(i, 258)
                }
                NORTH_EAST_BUTTON -> printFunc("dx")
                NORTH_WEST_BUTTON -> printFunc("∫ab")

            }
        }

        val c1 = { action: Int ->
            when (action) {
                MAIN -> print("1")
                NORTH_WEST_BUTTON -> printFunc("cos")
                SOUTH_WEST_BUTTON -> printFunc("acos")
            }
        }

        val c2 = { action: Int ->
            when (action) {
                MAIN -> print("2")
                NORTH_WEST_BUTTON -> printFunc("sin")
                SOUTH_WEST_BUTTON -> printFunc("asin")
            }
        }

        val c3 = { action: Int ->
            when (action) {
                MAIN -> print("3")
                NORTH_WEST_BUTTON -> printFunc("tan")
                NORTH_EAST_BUTTON -> printFunc("cot")
                SOUTH_WEST_BUTTON -> printFunc("atan")
                SOUTH_EAST_BUTTON -> printFunc("acot")
            }
        }


        val c4 = { action: Int ->
            when (action) {
                MAIN -> print("*")
                NORTH_WEST_BUTTON -> print("^2")
                NORTH_EAST_BUTTON -> printFunc("∛")
                SOUTH_EAST_BUTTON -> printFunc("√")
                SOUTH_WEST_BUTTON -> print("^")
            }
        }

        val c5 = { action: Int ->
            when (action) {
                MAIN -> {
                    val i = Intent(this@MainActivity, ConstantActivity::class.java)
                    startActivityForResult(i, 258)
                }
            }
        }

        val d1 = { action: Int ->
            when (action) {
                MAIN -> print("(")
                NORTH_EAST_BUTTON -> {
                    val s = "(${expression.text})"
                    expression.setText(s)
                    expression.setSelection(0)
                    onChange()
                }
            }
        }

        val d2 = { action: Int ->
            when (action) {
                MAIN -> print("0")
                NORTH_WEST_BUTTON -> print("00")
                SOUTH_WEST_BUTTON -> print("000")
            }
        }

        val d3 = { action: Int ->
            when (action) {
                MAIN -> print(")")
                NORTH_WEST_BUTTON -> {
                    val s = "(${expression.text})"
                    expression.setText(s)
                    expression.setSelection(s.lastIndex + 1)
                    onChange()
                }
            }
        }

        val d4 = { action: Int ->
            when (action) {
                MAIN -> print("/")
                NORTH_WEST_BUTTON -> {
                    val s = "1/(${expression.text})"
                    expression.setText(s)
                    expression.setSelection(s.lastIndex)
                    onChange()
                }
            }
        }

        val d5 = { action: Int ->
            when (action) {
                MAIN -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        val e1 = { action: Int ->
            when (action) {
                MAIN -> {
                    if (expression.selectionStart > 0)
                        expression.setSelection(expression.selectionStart - 1)
                }
                NORTH_EAST_BUTTON -> expression.setSelection(0)
            }
        }

        val e2 = { action: Int ->
            when (action) {
                MAIN -> {
                    if (expression.text.toString().lastIndex + 1 != expression.selectionStart)
                        expression.setSelection(expression.selectionStart + 1)
                }
                NORTH_WEST_BUTTON -> expression.setSelection(expression.text.toString().lastIndex + 1)
            }
        }

        val e3 = { action: Int ->
            when (action) {
                MAIN -> print(".")
                NORTH_WEST_BUTTON -> print(", ")
            }
        }

        val e4 = { action: Int ->
            when (action) {
                MAIN -> {
                    Settings.FormatNumber.rationalMode = !Settings.FormatNumber.rationalMode
                    if (Settings.FormatNumber.rationalMode) {
                        showToast("Режим рациональных чисел включен")
                    } else
                        showToast("Режим рациональных чисел выключен")
                    onChange(false)
                }
            }
        }

        val e5 = e5@{ action: Int ->
            when (action) {
                MAIN -> {
                    val (text, pos) = History.undo() ?: return@e5
                    expression.setText(text)
                    expression.setSelection(pos)
                    onChange(false)
                }

                NORTH_WEST_BUTTON -> {
                    val (text, pos) = History.redo() ?: return@e5
                    expression.setText(text)
                    expression.setSelection(pos)
                    onChange(false)
                }
            }
        }
    }
}
