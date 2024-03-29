/*
 * Copyright 2023 Lingshot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lingshot.screencapture.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import com.lingshot.screencapture.navigation.NavigationIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

class ScreenCaptureManager @Inject constructor(
    private val context: Context,
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
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {}, null)
        imageReader = ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            android.graphics.PixelFormat.RGBA_8888,
            2,
        )
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_NAME_DISPLAY,
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            displayMetrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader?.surface,
            null,
            null,
        )
    }

    fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.setOnImageAvailableListener(null, null)
        mediaProjection?.stop()
        mediaProjection = null
    }

    fun captureScreenshot(coroutineScope: CoroutineScope): Bitmap? {
        val image: Image? = imageReader?.acquireLatestImage()
        val bitmap: Bitmap? = image?.let { imageToBitmap(it) }
        image?.close()
        saveBitmap(bitmap, coroutineScope)
        return bitmap
    }

    private fun imageToBitmap(image: Image): Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * displayMetrics.widthPixels

        val bitmap = Bitmap.createBitmap(
            displayMetrics.widthPixels + rowPadding / pixelStride,
            displayMetrics.heightPixels,
            Bitmap.Config.ARGB_8888,
        )

        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
        )
    }

    private fun saveBitmap(bitmap: Bitmap?, coroutineScope: CoroutineScope) {
        if (bitmap == null) return

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val directory = directoryScreenShot()
                    val fileName = "Lingshot_${System.currentTimeMillis()}.jpg"
                    val file = File(directory, fileName)

                    val fos = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                    fos.close()
                    NavigationIntent.launchScreenShotActivity(
                        context,
                        Uri.fromFile(file),
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun directoryScreenShot(): File {
        val directory = context.getDir(DIRECTORY_NAME, Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return directory
    }

    companion object {
        private const val DIRECTORY_NAME = "screenshot"
        private const val VIRTUAL_NAME_DISPLAY = "ScreenCapture"
    }
}
