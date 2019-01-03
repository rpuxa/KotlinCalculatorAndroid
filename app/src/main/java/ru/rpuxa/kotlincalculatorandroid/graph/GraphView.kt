package ru.rpuxa.kotlincalculatorandroid.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.rpuxa.kotlincalculatorandroid.Settings
import java.lang.Math.min
import java.lang.Math.round
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sqrt

const val DARK_GREY = 0x1f_ff_ff_ff

const val GRAPH_BUNDLE = "graph"

const val SENSITIVITY_ZOOM = 2f



class GraphView(context: Context, attrs: AttributeSet) : View(context, attrs), View.OnTouchListener {
    private val camera = Camera()
    private val objects = ArrayList<GraphObj>()

    init {
        super.setOnTouchListener(this)
        objects.add(Axises())
        resolution = 100
    }

    companion object {
        const val BEST_FPS = 25f
        const val MAX_RESOLUTION = 300
        const val MIN_RESOLUTION = 30
        var resolution = 100
    }

    @Volatile
    private var fps = null as Float?
    @Volatile
    private var lastTimeFrame = null as Long?
    @Volatile
    private var maxQuality = false

    override fun onDraw(canvas: Canvas?) {
        canvas!!
        val tempResolution = resolution
        if (maxQuality)
            resolution = MAX_RESOLUTION
        canvas.drawARGB(255, 0, 0, 0)
        camera.canvas = canvas
        camera.viewHeight = height.toFloat()
        camera.viewWidth = width.toFloat()
        camera.width = width.toFloat() / height.toFloat() * camera.height
        for (obj in objects)
            obj.draw(camera)
        camera.paint.color = Color.GRAY
        canvas.drawText("steps: $resolution", 2f, (height - 20).toFloat(), camera.paint)


        if (lastTimeFrame != null && !maxQuality) {
            fps = 1000f / (System.currentTimeMillis() - lastTimeFrame!!)
            val delta = abs(fps!! - BEST_FPS).toInt()
            if (fps!! < BEST_FPS) {
                resolution -= delta
                if (resolution < MIN_RESOLUTION)
                    resolution = MIN_RESOLUTION
            } else {
                resolution += delta
                if (resolution > MAX_RESOLUTION)
                    resolution = MAX_RESOLUTION
            }
        }
        if (maxQuality)
            resolution = tempResolution
        else
            lastTimeFrame = System.currentTimeMillis()

        maxQuality = false
    }

    @Volatile
    private var lastTouch0 = null as Array<Float>?
    @Volatile
    private var lastTouch1 = null as Array<Float>?



    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && lastTouch1 == null) {
                    if (lastTouch0 != null) {
                        camera.x += (lastTouch0!![0] - event.x) / width * camera.width
                        camera.y -= (lastTouch0!![1] - event.y) / height * camera.height
                        invalidate()
                    }
                    lastTouch0 = arrayOf(event.x, event.y)
                } else if (event.pointerCount == 2) {
                    if (lastTouch0 != null && lastTouch1 != null) {
                        val calcDist = { x1: Float, y1: Float, x2: Float, y2: Float -> sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) }
                        val dist = calcDist(lastTouch0!![0], lastTouch0!![1], lastTouch1!![0], lastTouch1!![1])
                        val newDist = dist - calcDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                        camera.height += newDist / height * camera.height * SENSITIVITY_ZOOM
                        camera.width += newDist / width * camera.width * SENSITIVITY_ZOOM
                        invalidate()
                    }
                    lastTouch0 = arrayOf(event.getX(0), event.getY(0))
                    lastTouch1 = arrayOf(event.getX(1), event.getY(1))
                }
            }

            MotionEvent.ACTION_UP -> {
                lastTouch0 = null
                lastTouch1 = null
                maxQuality = true
                invalidate()
                lastTimeFrame = null
            }
        }
        return true
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
    }

    fun addGraph(function: (Float) -> Float, diff: (Float) -> Float) {
        objects.add(Graph(function, diff))
    }

    fun removeAllGraph() {
        for (i in objects.indices.reversed())
            if (objects[i] is Graph)
                objects.removeAt(i)
    }

    class Camera {
        var x = 0f
        var y = 0f
        var height = 10f
        var width = 0f
        var resolution = 20
        val paint = Paint()
        lateinit var canvas: Canvas
        var viewHeight = null as Float?
        var viewWidth = null as Float?

        val left
            get() = x - width / 2

        val right
            get() = x + width / 2

        val top
            get() = y + height / 2

        val bottom
            get() = y - height / 2

        init {
            paint.color = Color.WHITE
            paint.isAntiAlias = true
            paint.textSize = 20f
        }


        fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = 1f) {
            val fl = x1.canvasCoordinatesW()
            val fl1 = y1.canvasCoordinatesH()
            val fl2 = x2.canvasCoordinatesW()
            val fl3 = y2.canvasCoordinatesH()

            paint.strokeWidth = width

            canvas.drawLine(fl, fl1, fl2, fl3, paint)
        }

        fun drawTextYCenter(text: String, x: Float, y: Float) {
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            canvas.drawText(text, x.canvasCoordinatesW(), y.canvasCoordinatesH() + rect.height() / 2, paint)
        }

        fun drawTextXCenter(text: String, x: Float, y: Float) {
            canvas.drawText(text, x.canvasCoordinatesW() - paint.measureText(text) / 2, y.canvasCoordinatesH(), paint)
        }

        private fun Float.canvasCoordinatesW() = (this - x + width / 2) / width * viewWidth!!

        private fun Float.canvasCoordinatesH() = viewHeight!! - (this - y + height / 2) / height * viewHeight!!

        private fun atScreen(x: Float, y: Float) = x >= this.x - width / 2 && y >= this.y - height / 2 && x <= this.x + width / 2 && y <= this.y + height / 2


    }

    interface GraphObj {
        fun draw(camera: Camera)
    }

    class Axises : GraphObj {
        override fun draw(camera: Camera) {
            with(camera) {
                val cost = (height / resolution).roundD()

                var i = bottom.nearest(cost)
                while (i < top) {
                    paint.color = Color.WHITE
                    drawLine(-width / 70, i, width / 70, i)
                    val cutted = i.cut()
                    if (cutted != 0f) {
                        val fl = width / 40
                        var x1 = fl
                        if (left + fl > x1)
                            x1 = left + fl
                        else if (right - 2 * fl < x1)
                            x1 = right - 2 * fl
                        drawTextYCenter("$cutted", x1, i)
                        paint.color = DARK_GREY
                        drawLine(left, i, right, i)
                    }
                    i += cost
                }

                paint.color = Color.WHITE
                drawLine(0f, bottom, 0f, top)
            }

            with(camera) {
                val cost = (height / resolution).roundD()

                var i = left.nearest(cost)
                while (i < right) {
                    paint.color = Color.WHITE
                    drawLine(i, -width / 70, i, width / 70)
                    val cutted = i.cut()
                    if (cutted != 0f) {
                        val fl = -height / 40
                        var y1 = fl
                        if (top + fl < y1)
                            y1 = top + fl
                        else if (bottom - 2 * fl > y1)
                            y1 = bottom - 2 * fl
                        drawTextXCenter("$cutted", i, y1)
                        paint.color = DARK_GREY
                        drawLine(i, bottom, i, top)
                    }
                    i += cost
                }

                paint.color = Color.WHITE
                drawLine(left, 0f, right, 0f)
            }
        }

        private fun Float.cut() = BigDecimal(this.toDouble()).setScale(4, RoundingMode.HALF_EVEN).toFloat()


        private fun Float.roundD(): Float {
            var i = 0
            while (true) {
                val n1 = if (this < 1) 1 / getNum(i) else getNum(i)
                val n2 = if (this < 1) 1 / getNum(i + 1) else getNum(i + 1)

                if (this > n1 != this > n2) {
                    return if (abs(this - n1) < abs(this - n2)) n1 else n2
                }
                i++
            }
        }

        private fun Float.nearest(div: Float) = round(this / div) * div

        private val num = arrayOf(1, 2, 5)

        private fun getNum(i: Int) = (num[i % 3] * Math.pow(10.0, (i / 3).toDouble())).toFloat()
    }

    class Graph(val function: (Float) -> Float, val diff: (Float) -> Float) : GraphObj {

        override fun draw(camera: Camera) {
            with(camera) {
                paint.color = 0xff000000.toInt() or Settings.Graphics.color
                var i = left
                var step: Float
                var y1 = function(i)
                var x1 = i
             //   var x1Diff = diff(x1)
                while (i <= right) {
                    val x2 = i
                    val y2 = function(x2)
                    val x2Diff = diff(x2)
                    step = width / ((GraphView.resolution * 3 / 2 * min(abs(x2Diff), 40f) / 40f + GraphView.resolution / 2))
                    if (step.isNaN())
                        step = width / GraphView.resolution

                    if (abs(y1 - y2) < 6)
                        drawLine(x1, y1, x2, y2, Settings.Graphics.lineWidth)

                    y1 = y2
                    x1 = x2
                   // x1Diff = x2Diff
                    i += step
                }
            }
        }
    }
}