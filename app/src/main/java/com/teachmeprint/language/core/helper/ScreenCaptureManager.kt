package com.teachmeprint.language.core.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import com.teachmeprint.language.core.util.NavigationIntentUtil
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ScreenCaptureManager @Inject constructor(
    private val context: Context,
    private val notificationClearScreenshot: NotificationClearScreenshot
) {

    private val mediaProjectionManager: MediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val displayMetrics by lazy { context.resources.displayMetrics }

    @SuppressLint("WrongConstant")
    fun startCapture(resultCode: Int, data: Intent) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        imageReader = ImageReader.newInstance(
            displayMetrics.widthPixels, displayMetrics.heightPixels,
            android.graphics.PixelFormat.RGBA_8888, 2
        )
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_NAME_DISPLAY,
            displayMetrics.widthPixels, displayMetrics.heightPixels,
            displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.setOnImageAvailableListener(null, null)
        mediaProjection?.stop()
        mediaProjection = null
    }

    fun captureScreenshot(rect: Rect): Bitmap? {
        val image: Image? = imageReader?.acquireLatestImage()
        val bitmap: Bitmap? = image?.let { imageToBitmap(it, rect) }
        image?.close()
        saveBitmap(bitmap)
        return bitmap
    }

    private fun imageToBitmap(image: Image, cropRect: Rect): Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val cropWidth = cropRect.width()
        val cropHeight = cropRect.height()
        val cropOffsetX = cropRect.left * pixelStride
        val cropOffsetY = cropRect.top * rowStride
        val rowPadding = rowStride - cropWidth * pixelStride

        val croppedBitmap = Bitmap.createBitmap(
            cropWidth, cropHeight, Bitmap.Config.ARGB_8888
        )

        val croppedBuffer = ByteBuffer.allocate(cropWidth * cropHeight * 4)
        buffer.position(cropOffsetY + cropOffsetX)

        for (y in 0 until cropHeight) {
            for (x in 0 until cropWidth) {
                val pixel = buffer.int
                croppedBuffer.putInt(pixel)
            }
            buffer.position(buffer.position() + rowPadding)
        }

        croppedBuffer.rewind()
        croppedBitmap.copyPixelsFromBuffer(croppedBuffer)
        return croppedBitmap
    }

    private fun saveBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Screenshot_$timeStamp.jpg"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            fileName
        )
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            if (file.exists()
                && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            ) {
                notificationClearScreenshot.start()
                context.let { NavigationIntentUtil.launchScreenShotActivity(it, file.absolutePath) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val VIRTUAL_NAME_DISPLAY = "ScreenCapture"
    }
}