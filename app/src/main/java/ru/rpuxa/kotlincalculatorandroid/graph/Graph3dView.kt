package ru.rpuxa.kotlincalculatorandroid.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val ROTATE_SENSITIVITY = 0.005f

class Graph3dView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet), View.OnTouchListener {
    private val camera = Camera()
    private val objects = ArrayList<Graph3dObj>()

    init {
        super.setOnTouchListener(this)
        objects.add(Axises())
        addGraph { x, y -> 2 * cos(x) * sin(y)}
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas!!
        canvas.drawARGB(255, 0, 0, 0)
        camera.width = camera.height / height * width
        camera.canvas = canvas
        camera.viewHeight = height.toFloat()
        camera.viewWidth = width.toFloat()

        for (obj in objects)
            obj.draw(camera)

        camera.clear()
    }

    fun addGraph(function: (Float, Float) -> Float) {
        objects.add(Graph3d(function))
    }

    fun removeAllGraph() {
        for (i in objects.indices.reversed())
            if (objects[i] is Graph3d)
                objects.removeAt(i)
    }

    var lastTouch = null as Array<Float>?

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_MOVE -> {
                if (lastTouch != null) {
                    camera.rotateAroundZ(lastTouch!![0] - event.x)
                    invalidate()
                }
                lastTouch = arrayOf(event.x, event.y)
            }

            MotionEvent.ACTION_UP -> {
                lastTouch = null
            }
        }
        return true
    }

    private class Camera {
        var center: Point3d
        var focus: Point3d
        var height = 4f
        var width = 2f
        var focusDist = 5f

        var viewHeight = null as Float?
        var viewWidth = null as Float?

        val paint = Paint()
        lateinit var canvas: Canvas

        constructor(center: Point3d, lookAt: Point3d, focusDist: Float) {
            this.center = center
            this.focusDist = focusDist
            focus = lookAt.getPoint(center, focusDist)
        }

        constructor(center: Point3d, focusDist: Float) : this(center, Point3d(), focusDist)

        constructor() : this(Point3d(10f, -2f, 10f), 5f)

        var bottomLine0 = null as ParallelLine?

        fun getBottomLine(): ParallelLine {
            if (bottomLine0 != null)
                return bottomLine0!!

            val screenPlane = getScreenPlane()
            val d = (height / 2).sqr()
            val c = screenPlane.c
            val p = screenPlane.a * center.x + screenPlane.b * center.y + screenPlane.d
            val n = screenPlane.a.sqr() + screenPlane.b.sqr()
            val z1 = center.z

            val z = (-sqrt((c.sqr() * d - c.sqr() * z1.sqr() - 2 * c * p * z1 + d * n - p.sqr()) / n) - c * p / n + z1) / (c.sqr() / n + 1)

            val ans = ParallelLine(screenPlane.a, screenPlane.b, screenPlane.c * z + screenPlane.d, z)
            bottomLine0 = ans
            return ans
        }

        var topLine0 = null as ParallelLine?

        fun getTopLine(): ParallelLine {
            if (topLine0 != null)
                return topLine0!!

            val screenPlane = getScreenPlane()
            val d = (height / 2).sqr()
            val c = screenPlane.c
            val p = screenPlane.a * center.x + screenPlane.b * center.y + screenPlane.d
            val n = screenPlane.a.sqr() + screenPlane.b.sqr()
            val z1 = center.z

            val z = (sqrt((c.sqr() * d - c.sqr() * z1.sqr() - 2 * c * p * z1 + d * n - p.sqr()) / n) - c * p / n + z1) / (c.sqr() / n + 1)

            val ans = ParallelLine(screenPlane.a, screenPlane.b, screenPlane.c * z + screenPlane.d, z)
            topLine0 = ans
            return ans
        }

        fun getLeftPoint(line: ParallelLine): Point3d {
            val a = focus.y - center.y
            val b = center.x - focus.x
            val c = -a * center.x - b * center.y
            val y = (c * line.a - a * line.c) / (line.b * a - line.a * b)
            val x = if (abs(a) > abs(line.a)) -(c + b * y) / a else -(line.c + line.b * y) / line.a
            val centr = Point3d(focus.x, focus.y).getPoint(Point3d(x, y), width / 2)
            centr.x -= x
            centr.y -= y
            return Point3d(-centr.y + x, centr.x + y, line.z)
        }

        fun getRightPoint(line: ParallelLine): Point3d {
            val a = focus.y - center.y
            val b = center.x - focus.x
            val c = -a * center.x - b * center.y
            val y = (c * line.a - a * line.c) / (line.b * a - line.a * b)
            val x = if (abs(a) > abs(line.a)) -(c + b * y) / a else -(line.c + line.b * y) / line.a
            val centr = Point3d(focus.x, focus.y).getPoint(Point3d(x, y), width / 2)
            centr.x -= x
            centr.y -= y
            return Point3d(centr.y + x, -centr.x + y, line.z)
        }

        var leftLine0 = null as Line?

        fun getLeftLine(): Line {
            if (leftLine0 != null)
                return leftLine0!!
            val ans = Line(getLeftPoint(getBottomLine()), getLeftPoint(getTopLine()))
            leftLine0 = ans
            return ans
        }

        var rightLine0 = null as Line?

        fun getRightLine(): Line {
            if (rightLine0 != null)
                return rightLine0!!
            val ans = Line(getRightPoint(getBottomLine()), getRightPoint(getTopLine()))
            rightLine0 = ans
            return ans
        }

        var screenPlane0 = null as Plane?

        fun getScreenPlane(): Plane {
            if (screenPlane0 != null)
                return screenPlane0!!
            val ans = Plane.fromNormalAndPoint(center, focus)
            screenPlane0 = ans
            return ans
        }

        fun getProjectionPoint(point3d: Point3d): Point3d? {
            val line = Line(focus, point3d)
            val point = getScreenPlane().collision(line)
            if (focus.dist(point3d) < point.dist(point3d))
                return null
            val bottomDist = getBottomLine().dist(point)
            if (bottomDist > height)
                return null
            val topDist = getTopLine().dist(point)
            if (topDist > height)
                return null
            val leftDist = getLeftLine().dist(point)
            if (leftDist > width)
                return null
            val rightDist = getRightLine().dist(point)
            if (rightDist > width)
                return null
            return Point3d(leftDist, bottomDist)
        }

        fun drawLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
            val point0 = getProjectionPoint(Point3d(x1, y1, z1)) ?: return
            val point1 = getProjectionPoint(Point3d(x2, y2, z2)) ?: return
            val canvasCoordinatesW = point0.x.canvasCoordinatesW()
            val canvasCoordinatesH = point0.y.canvasCoordinatesH()
            val canvasCoordinatesW1 = point1.x.canvasCoordinatesW()
            val canvasCoordinatesH1 = point1.y.canvasCoordinatesH()
            canvas.drawLine(canvasCoordinatesW, canvasCoordinatesH, canvasCoordinatesW1, canvasCoordinatesH1, paint)
        }

        private fun Float.canvasCoordinatesW() = this / width * viewWidth!!

        private fun Float.canvasCoordinatesH() = viewHeight!! - this / height * viewHeight!!

        fun clear() {
            bottomLine0 = null
            topLine0 = null
            leftLine0 = null
            rightLine0 = null
            screenPlane0 = null
        }

        fun rotateAroundZ(rads: Float) {
            val rads0 = rads * ROTATE_SENSITIVITY
            val center = Point3d(
                    this.center.x * cos(rads0) - this.center.y * sin(rads0),
                    this.center.x * sin(rads0) + this.center.y * cos(rads0),
                    this.center.z
            )
            this.center = center
            focus = Point3d().getPoint(center, focusDist)
            clear()
        }
    }

    private class Point3d(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
        fun dist(point3d: Point3d) = sqrt((point3d.x - x).sqr() + (point3d.y - y).sqr() + (point3d.z - z).sqr())

        fun getPoint(end: Point3d, dist: Float): Point3d {
            val k = 1 + dist / dist(end)
            return Point3d(k * (end.x - x) + x, k * (end.y - y) + y, k * (end.z - z) + z)
        }

    }

    private class Line(var x1: Float, var x2: Float, var y1: Float, var y2: Float, var z1: Float, var z2: Float) {

        constructor(first: Point3d, second: Point3d) : this(-second.x, first.x - second.x, -second.y, first.y - second.y, -second.z, first.z - second.z)

        fun dist(point3d: Point3d): Float {
            val v1 = Point3d(x2, y2, z2)
            val v2 = Point3d(point3d.x + x1, point3d.y + y1, point3d.z + z1)
            val v3 = Point3d(
                    v1.y * v2.z - v1.z * v2.y,
                    v1.z * v2.x - v1.x * v2.z,
                    v1.x * v2.y - v1.y * v2.x
            )

            return sqrt((v3.x.sqr() + v3.y.sqr() + v3.z.sqr()) / (v1.x.sqr() + v1.y.sqr() + v1.z.sqr()))
        }
    }

    private class Plane(var a: Float = 0f, var b: Float = 0f, var c: Float = 0f, var d: Float = 0f) {

        fun collision(line: Line): Point3d {
            val fl = a * line.x1 + b * line.y1 + c * line.z1 - d
            val fl1 = a * line.x2 + b * line.y2 + c * line.z2
            val t = fl / fl1
            return Point3d(-line.x1 + line.x2 * t, -line.y1 + line.y2 * t, -line.z1 + line.z2 * t)
        }

        companion object {
            fun from3Points(points: Array<Point3d>): Plane {
                val a = ((points[1].y - points[0].y) * (points[2].z - points[0].z) - (points[1].z - points[0].z) * (points[2].y - points[0].y))
                val b = ((points[1].z - points[0].z) * (points[2].x - points[0].x) - (points[1].x - points[0].x) * (points[2].z - points[0].z))
                val c = ((points[1].x - points[0].x) * (points[2].y - points[0].y) - (points[1].y - points[0].y) * (points[2].x - points[0].x))
                val d = -(points[0].x * a + points[0].y * b + points[0].z * c)
                return Plane(a, b, c, d)
            }

            fun fromNormalAndPoint(start: Point3d, end: Point3d): Plane {
                val a = end.x - start.x
                val b = end.y - start.y
                val c = end.z - start.z
                val d = -(a * start.x + b * start.y + c * start.z)
                return Plane(a, b, c, d)
            }
        }
    }

    private class ParallelLine(var a: Float, var b: Float, var c: Float, var z: Float) {

        fun dist(point: Point3d): Float {
            val sqr = (a * point.x + b * point.y + c).sqr() / (a.sqr() + b.sqr())
            return sqrt(sqr + (z - point.z).sqr())
        }

    }

    private interface Graph3dObj {
        fun draw(camera: Camera)
    }

    private class Axises : Graph3dObj {
        val directions = arrayOf(
                arrayOf(1f, 0f, 0f),
                arrayOf(0f, 1f, 0f),
                arrayOf(0f, 0f, 1f)
        )

        override fun draw(camera: Camera) {
            with(camera) {
                paint.color = Color.WHITE
                for (dir in directions) {
                    var i = 0f
                    while (i < 10f) {
                        drawLine(i * dir[0], i * dir[1], i * dir[2], (i + 1) * dir[0], (i + 1) * dir[1], (i + 1) * dir[2])
                        i += .1f
                    }
                }
            }

        }
    }

    private class Graph3d(val function: (Float, Float) -> Float) : Graph3dObj {

        val resolution = 40f

        override fun draw(camera: Camera) {
            with(camera) {
                paint.color = Color.WHITE
                var x = -5f
                val step = 10 / resolution
                while (x < 5f) {
                    var y = -5f
                    while (y < 5f) {
                        drawLine(x, y, function(x, y), x, y + step, function(x, y + step))
                        y += step
                    }
                    x += step
                }
            }

            with(camera) {
                paint.color = Color.WHITE
                var y = -5f
                val step = 10 / resolution
                while (y < 5f) {
                    var x = -5f
                    while (x < 5f) {
                        drawLine(x, y, function(x, y), x + step, y , function(x + step, y))
                        x += step
                    }
                    y += step
                }
            }
        }

    }

}

fun Float.sqr() = this * this
