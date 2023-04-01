package com.teachmeprint.language.feature.screenshot.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class CropRectangleView(context: Context) : View(context) {

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRect: Rect = Rect()

    private var mStartX = 0f
    private var mStartY = 0f
    private var mEndX = 0f
    private var mEndY = 0f

    init {
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(mRect, mPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
                mEndX = mStartX
                mEndY = mStartY
                mRect.set(
                    mStartX.toInt(),
                    mStartY.toInt(),
                    mEndX.toInt(),
                    mEndY.toInt()
                )
            }
            MotionEvent.ACTION_MOVE -> {
                mEndX = event.x
                mEndY = event.y
                mRect.set(
                    mStartX.coerceAtMost(mEndX).toInt(),
                    mStartY.coerceAtMost(mEndY).toInt(),
                    mStartX.coerceAtLeast(mEndX).toInt(),
                    mStartY.coerceAtLeast(mEndY).toInt()
                )
                invalidate()
            }
        }
        return false
    }

    fun getRectCrop(): Rect {
        return mRect
    }
}