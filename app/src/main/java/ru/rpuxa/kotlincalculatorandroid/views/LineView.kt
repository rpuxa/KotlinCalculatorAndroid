package ru.rpuxa.kotlincalculatorandroid.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import ru.rpuxa.kotlincalculatorandroid.Settings

class LineView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var lineWidth = Settings.Graphics.lineWidth

    private var color = Settings.Graphics.color

    private val paint = Paint()

    override fun onDraw(canvas: Canvas?) {
        canvas!!
        paint.strokeWidth = lineWidth
        paint.color = 0xff000000.toInt() or color
        canvas.drawARGB(255, 0, 0, 0)
        val indent = width * .95f
        canvas.drawLine(indent, height / 2f, width - indent, height / 2f, paint)
    }

    fun changeWidth(width: Float) {
        lineWidth = width
        Settings.Graphics.lineWidth = width
        invalidate()
    }

    fun changeColor(color: Int) {
        this.color = color
        Settings.Graphics.color = color
        invalidate()
    }
}