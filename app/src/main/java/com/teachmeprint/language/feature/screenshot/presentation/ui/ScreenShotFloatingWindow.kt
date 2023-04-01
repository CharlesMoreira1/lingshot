@file:Suppress("Deprecation", "InflateParams", "ClickableViewAccessibility")

package com.teachmeprint.language.feature.screenshot.presentation.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.view.isVisible
import com.teachmeprint.language.R
import com.teachmeprint.language.core.util.isViewOverlapping
import com.teachmeprint.language.feature.screenshot.presentation.FloatingView
import kotlinx.coroutines.*
import javax.inject.Inject

class ScreenShotFloatingWindow @Inject constructor(private val context: Context) {

    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val rootViewFloating by lazy {
        LayoutInflater.from(context).inflate(R.layout.floating_screen_shot_window_layout, null)
    }

    private val rootViewFloatingClose by lazy {
        LayoutInflater.from(context).inflate(R.layout.floating_close_window_layout, null)
    }

    private val rootViesssssswFloatingClose by lazy {
        FloatingView(context)
    }

    private lateinit var windowParamsFloating: WindowManager.LayoutParams
    private lateinit var windowParamsFloatingClose: WindowManager.LayoutParams
    private lateinit var aaaaaaaWindow: WindowManager.LayoutParams

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val imageButtonScreenShotFloating by lazy {
        rootViewFloating.findViewById<ImageButton>(R.id.image_button_screen_shot_floating)
    }

    init {
        setupWindowParamsFloating()
        setupWindowParamsFloatingClose()
    }

    fun onFloating(
        coroutineScope: CoroutineScope,
        onScreenShot: (Rect) -> Unit,
        onStopService: () -> Unit
    ) {
        with(imageButtonScreenShotFloating) {
            setOnClickListener {
                showOrHide(false)
                coroutineScope.launch {
                    delay(100L)
                    withContext(Dispatchers.IO) {
                        onScreenShot.invoke(rootViesssssswFloatingClose.getRectCrop())
                    }
                    showOrHide()
                    rootViesssssswFloatingClose.clearDrawing()
                }
            }
            onTouchMoveFloatingButton(onStopService)
        }
    }

    private fun ImageButton.onTouchMoveFloatingButton(onStopService: () -> Unit) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        setOnTouchListener { _, event ->
            event.setTouchSensitivity()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParamsFloating.x
                    initialY = windowParamsFloating.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                }
                MotionEvent.ACTION_MOVE -> {
                    windowParamsFloating.x = (initialX + event.rawX - initialTouchX).toInt()
                    windowParamsFloating.y = (initialY + event.rawY - initialTouchY).toInt()

                    if (windowParamsFloating.x < 0) windowParamsFloating.x = 0
                    if (windowParamsFloating.y < 0) windowParamsFloating.y = 0
                    if (windowParamsFloating.x > screenWidth - rootViewFloating.width) windowParamsFloating.x =
                        screenWidth - rootViewFloating.width
                    if (windowParamsFloating.y > screenHeight - rootViewFloating.height) windowParamsFloating.y =
                        screenHeight - rootViewFloating.height
                    if (event.rawY > initialTouchY) rootViewFloatingClose.isVisible = true
                    windowManager.updateViewLayout(rootViewFloating, windowParamsFloating)
                }
                MotionEvent.ACTION_UP -> {
                    rootViewFloatingClose.isVisible = false
                    setupFloatingCloseService(onStopService)
                }
            }
            false
        }
    }

    private fun MotionEvent.setTouchSensitivity() {
        setLocation(x * 1.0f, y * 1.0f)
    }

    private fun setupWindowParamsFloating() {
        aaaaaaaWindow = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowParamsFloating = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
    }

    private fun setupWindowParamsFloatingClose() {
        windowParamsFloatingClose = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER or Gravity.BOTTOM
            x = 0
            y = 0
        }
    }

    private fun setupFloatingCloseService(onStopService: () -> Unit) {
        if (rootViewFloating.isViewOverlapping(rootViewFloatingClose)) {
            onStopService.invoke()
        }
    }

    fun showOrHide(isVisible: Boolean = true) {
        rootViewFloating.isVisible = isVisible
        rootViesssssswFloatingClose.isVisible = isVisible
    }

    fun start() = runCatching {
        windowManager.addView(rootViesssssswFloatingClose, aaaaaaaWindow)
        windowManager.addView(rootViewFloating, windowParamsFloating)
        windowManager.addView(rootViewFloatingClose, windowParamsFloatingClose)

    }.getOrNull()

    fun close() = runCatching {
        windowManager.removeView(rootViewFloating)
        windowManager.removeView(rootViewFloatingClose)
        windowManager.removeView(rootViesssssswFloatingClose)
    }.getOrNull()
}