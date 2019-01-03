package ru.rpuxa.kotlincalculatorandroid.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import ru.rpuxa.kotlincalculatorandroid.R
import kotlin.math.min
import kotlin.math.sqrt

const val GAP_WIDTH = .1f
const val CIRCLE_RADIUS = .4f

const val NORTH_WEST = 0
const val NORTH_EAST = 1
const val SOUTH_EAST = 2
const val SOUTH_WEST = 3


const val MAIN = 0
const val NORTH_WEST_BUTTON = 1
const val NORTH_EAST_BUTTON = 2
const val SOUTH_EAST_BUTTON = 3
const val SOUTH_WEST_BUTTON = 4


class DragButton(context: Context, attributeSet: AttributeSet) : View(context, attributeSet), View.OnTouchListener, View.OnClickListener {

    var circleRadius = CIRCLE_RADIUS
    val paint = Paint()

    var mainBitmap = null as Bitmap?

    var listener: (Int) -> Unit = {}

    val minis = arrayOf(
            MiniButton(NORTH_WEST),
            MiniButton(NORTH_EAST),
            MiniButton(SOUTH_EAST),
            MiniButton(SOUTH_WEST)
    )

    var vHeight = height
    var vWidth = width
    var size = min(vHeight, vWidth).toFloat()


    init {
        super.setOnTouchListener(this)
        super.setOnClickListener(this)
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.DragButton)
        minis[NORTH_WEST].bitmap = BitmapFactory.decodeResource(resources, array.getResourceId(R.styleable.DragButton_north_west_image, 0))
        minis[NORTH_EAST].bitmap = BitmapFactory.decodeResource(resources, array.getResourceId(R.styleable.DragButton_north_east_image, 0))
        minis[SOUTH_EAST].bitmap = BitmapFactory.decodeResource(resources, array.getResourceId(R.styleable.DragButton_south_east_image, 0))
        minis[SOUTH_WEST].bitmap = BitmapFactory.decodeResource(resources, array.getResourceId(R.styleable.DragButton_south_west_image, 0))
        mainBitmap = BitmapFactory.decodeResource(resources, array.getResourceId(R.styleable.DragButton_main_image, 0))
        array.recycle()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        vWidth = w
        vHeight = h
        size = min(vHeight, vWidth).toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas0: Canvas?) {
        val canvas = canvas0!!
        if (vHeight == 0 || vWidth == 0)
            return
        size = min(vWidth, vHeight).toFloat()
        paint.color = Color.SQUARES_COLOR
        for (mini in minis)
            mini.draw(canvas)
        paint.color = Color.CIRCLE_COLOR
        canvas.drawCircle((vWidth / 2).toFloat(), (vHeight / 2).toFloat(), size * circleRadius, paint)
        val circleSize = (size * circleRadius).toInt()
        if (mainBitmap != null)
            canvas.drawBitmap(Bitmap.createScaledBitmap(mainBitmap, circleSize, circleSize, false), (vWidth - circleSize) / 2f, (vHeight - circleSize) / 2f, paint)
    }

    fun setCalculatorListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        size = min(vWidth, vHeight).toFloat()
    }

    var clicked = false
    var exited = false

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val centerX = vWidth / 2f
        val centerY = vHeight / 2f
        val dist = dist(centerX, centerY, event!!.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (clicked && dist > size * CIRCLE_RADIUS) {
                    exited(event)
                    clicked = false
                    exited = true
                    return true
                }
            }

            MotionEvent.ACTION_DOWN -> {
                if (dist <= size * CIRCLE_RADIUS) {
                    clicked = true
                }
                exited = false
            }

            MotionEvent.ACTION_UP -> {
                clicked = false
                return exited
            }
        }


        return false
    }

    fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))
 a
    override fun onClick(v: View) {
        val animator = ValueAnimator.ofFloat(.4f, .33f, .4f)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            circleRadius = it.animatedValue as Float
            invalidate()
        }
        animator.start()
        listener(0)
    }

    override fun setOnClickListener(l: OnClickListener?) {
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
    }

    fun exited(event: MotionEvent) {
        val xSign = (event.x - vWidth / 2) > 0
        val ySign = (event.y - vHeight / 2) > 0
        if (xSign) {
            if (ySign)
                minis[SOUTH_WEST].click()
            else
                minis[NORTH_WEST].click()
        } else {
            if (ySign)
                minis[SOUTH_EAST].click()
            else
                minis[NORTH_EAST].click()
        }
    }


    inner class MiniButton(val direction: Int) {

        var bitmap: Bitmap? = null

        var move = 0f

        fun click() {
            val animator = ValueAnimator.ofFloat(0f, 20f, 0f)
            animator.duration = 400
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                move = it.animatedValue as Float
                invalidate()
            }
            animator.start()
            listener(direction + 1)
        }

        fun draw(canvas: Canvas) {
            val centerX = vWidth / 2
            val centerY = vHeight / 2
            val halfSize = ((1 - GAP_WIDTH) / 4 + GAP_WIDTH / 2) * size
            val size = ((1 - GAP_WIDTH) / 2) * size
            when (direction) {
                NORTH_WEST -> {
                    canvas.drawSquare(centerX + halfSize + move, centerY - halfSize - move, size)
                    canvas.drawBitmap(vWidth - size + move, -move, size)
                }
                NORTH_EAST -> {
                    canvas.drawSquare(centerX - halfSize - move, centerY - halfSize - move, size)
                    canvas.drawBitmap(-move, -move, size)
                }
                SOUTH_EAST -> {
                    canvas.drawSquare(centerX - halfSize - move, centerY + halfSize + move, size)
                    canvas.drawBitmap(-move, vHeight - size + move, size)
                }
                SOUTH_WEST -> {
                    canvas.drawSquare(centerX + halfSize + move, centerY + halfSize + move, size)
                    canvas.drawBitmap(vWidth - size + move, vHeight - size + move, size)
                }
            }
        }

        fun Canvas.drawBitmap(left: Float, top: Float, size: Float) {
            if (bitmap != null) {
                val createScaledBitmap = Bitmap.createScaledBitmap(bitmap, size.toInt(), size.toInt(), false)
                this.drawBitmap(createScaledBitmap, left, top, paint)
            }
        }

    }

    fun Canvas.drawSquare(x: Float, y: Float, size: Float) {
        this.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, paint)
    }


}