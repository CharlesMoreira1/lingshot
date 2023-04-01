package com.teachmeprint.language.feature.screenshot.presentation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import java.lang.Math.max
import java.lang.Math.min

class FloatingView(context: Context) : View(context) {

    private var rectF = RectF()
    private var paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var startTouchX: Float = 0f
    private var startTouchY: Float = 0f
    private var endTouchX: Float = 0f
    private var endTouchY: Float = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startTouchX = event.x
                startTouchY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                endTouchX = event.x
                endTouchY = event.y
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        rectF.set(startTouchX, startTouchY, endTouchX, endTouchY)
        canvas?.drawRect(rectF, paint)
    }

    fun getRectCrop(): Rect {
        val left = min(startTouchX, endTouchX).toInt()
        val top = min(startTouchY, endTouchY).toInt()
        val right = max(startTouchX, endTouchX).toInt()
        val bottom = max(startTouchY, endTouchY).toInt()
        return Rect(left, top, right, bottom)
    }
}