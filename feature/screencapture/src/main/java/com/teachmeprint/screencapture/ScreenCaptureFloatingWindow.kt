@file:Suppress("Deprecation", "InflateParams", "ClickableViewAccessibility")

package com.teachmeprint.screencapture

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.view.isVisible
import com.teachmeprint.common.util.isViewOverlapping
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ScreenCaptureFloatingWindow @Inject constructor(private val context: Context) {

    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val rootViewFloating by lazy {
        LayoutInflater.from(context).inflate(R.layout.floating_screen_capture_window_layout, null)
    }

    private val rootViewFloatingClose by lazy {
        LayoutInflater.from(context).inflate(R.layout.floating_close_window_layout, null)
    }

    private lateinit var windowParamsFloating: WindowManager.LayoutParams
    private lateinit var windowParamsFloatingClose: WindowManager.LayoutParams

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val imageButtonScreenCaptureFloating by lazy {
        rootViewFloating.findViewById<ImageButton>(R.id.image_button_screen_capture_floating)
    }

    init {
        setupWindowParamsFloating()
        setupWindowParamsFloatingClose()
    }

    fun onFloating(
        coroutineScope: CoroutineScope,
        onScreenShot: () -> Unit,
        onStopService: () -> Unit
    ) {
        with(imageButtonScreenCaptureFloating) {
            setOnClickListener {
                showOrHide(false)
                coroutineScope.launch {
                    delay(100.milliseconds)
                    withContext(Dispatchers.IO) {
                        onScreenShot.invoke()
                    }
                    showOrHide()
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
                    if (windowParamsFloating.x > screenWidth - rootViewFloating.width) {
                        windowParamsFloating.x =
                            screenWidth - rootViewFloating.width
                    }
                    if (windowParamsFloating.y > screenHeight - rootViewFloating.height) {
                        windowParamsFloating.y =
                            screenHeight - rootViewFloating.height
                    }
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
        setLocation(x * TOUCH_SENSITIVITY, y * TOUCH_SENSITIVITY)
    }

    private fun setupWindowParamsFloating() {
        windowParamsFloating = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
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
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
    }

    private fun setupWindowParamsFloatingClose() {
        windowParamsFloatingClose = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
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
    }

    fun start() = runCatching {
        windowManager.addView(rootViewFloating, windowParamsFloating)
        windowManager.addView(rootViewFloatingClose, windowParamsFloatingClose)
    }.getOrNull()

    fun close() = runCatching {
        windowManager.removeView(rootViewFloating)
        windowManager.removeView(rootViewFloatingClose)
    }.getOrNull()

    companion object {
        private const val TOUCH_SENSITIVITY = 1.0F
    }
}
