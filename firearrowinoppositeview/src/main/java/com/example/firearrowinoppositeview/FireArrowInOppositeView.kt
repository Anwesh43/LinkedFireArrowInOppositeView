package com.example.firearrowinoppositeview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<Int> = arrayOf(
    "#3F51B5",
    "#4CAF50",
    "#FFC107",
    "#03A9F4",
    "#F44336"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 5
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val rot : Float = 90f
val arrowSizeFactor : Float = 4.5f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawArrow(sf1 : Float, sf2 : Float, size : Float, paint : Paint) {
    val arrowSize : Float = size / arrowSizeFactor
    drawLine(0f, 0f, size * sf1, 0f, paint)
    save()
    translate(size, 0f)
    for (j in 0..1) {
        save()
        rotate(45f * sf2 * (1f - 2 * j))
        drawLine(0f, 0f, -arrowSize * sf1, 0f, paint)
        restore()
    }
    restore()
}

fun Canvas.drawFireArrowInOpposite(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        rotate(90f * sf.divideScale(2, parts))
        translate(0f, h * 0.5f * sf.divideScale(3, parts))
        drawArrow(sf.divideScale(0, parts), sf.divideScale(1, parts), size, paint)
        restore()
    }
    restore()
}

fun Canvas.drawFAIONode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawFireArrowInOpposite(scale, w, h, paint)
}

class FireArrowInOppositeView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class FAIONode(var i : Int, val state : State = State()) {

        private var next : FAIONode? = null
        private var prev : FAIONode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = FAIONode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawFAIONode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : FAIONode {
            var curr : FAIONode? = prev
            if (dir == 1) {
               curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }
}