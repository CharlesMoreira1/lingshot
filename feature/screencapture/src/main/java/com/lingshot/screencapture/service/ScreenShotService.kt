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
@file:Suppress("Deprecation")

package com.lingshot.screencapture.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.lingshot.common.CommonConstant.CHANNEL_ID
import com.lingshot.common.helper.MainActivityManager.getMainActivity
import com.lingshot.screencapture.R
import com.lingshot.screencapture.ScreenCaptureFloatingWindow
import com.lingshot.screencapture.helper.ScreenCaptureManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class ScreenShotService : LifecycleService() {

    @Inject
    lateinit var screenCaptureFloatingWindow: ScreenCaptureFloatingWindow

    @Inject
    lateinit var screenCaptureManager: ScreenCaptureManager

    private var isOrientationPortrait: Boolean = true

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)

        isOrientationPortrait = if (configuration.orientation == ORIENTATION_PORTRAIT) {
            screenCaptureFloatingWindow.showOrHide()
            true
        } else {
            screenCaptureFloatingWindow.showOrHide(false)
            false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationForeground()
        setupScreenCaptureFloatingWindow()
        intent.sendIntentScreenCapture()

        if (intent?.action == STOP_SERVICE) {
            stopSelf()
        }
        setupFinishMainActivity()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setupScreenCaptureFloatingWindow() {
        screenCaptureFloatingWindow.start()
        screenCaptureFloatingWindow.onFloating(
            lifecycleScope,
            onScreenShot = {
                screenCaptureManager.captureScreenshot(lifecycleScope)
            },
        )
        screenCaptureFloatingWindow.onFloatingClose {
            lifecycleScope.launch {
                delay(500.milliseconds)
                stopSelf()
            }
        }
    }

    private fun Intent?.sendIntentScreenCapture() {
        val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getParcelableExtra(SCREEN_CAPTURE_DATA, Intent::class.java)
        } else {
            this?.getParcelableExtra(SCREEN_CAPTURE_DATA)
        }
        data?.let { screenCaptureManager.startCapture(RESULT_OK, it) }
    }

    private fun setupFinishMainActivity() {
        lifecycleScope.launch {
            try {
                delay(1.seconds)
                getMainActivity()?.finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupNotificationForeground() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.text_notification_title_display_display_tuned_on))
            .setContentIntent(intentMainActivity())
            .setContentText(
                getString(R.string.text_notification_message_display_reading_is_ready_to_use),
            )
            .setSmallIcon(R.drawable.ic_translate_24).run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(this@ScreenShotService, NOTIFICATION_FOREGROUND_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                } else {
                    startForeground(NOTIFICATION_FOREGROUND_ID, build())
                }
            }
    }

    private fun intentMainActivity() =
        Intent(this, Class.forName(MAIN_ACTIVITY_PATH)).run {
            PendingIntent.getActivity(
                this@ScreenShotService,
                0,
                this,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
            )
        }

    override fun onDestroy() {
        super.onDestroy()
        screenCaptureFloatingWindow.close()
        screenCaptureManager.stopCapture()
    }

    companion object {
        private const val MAIN_ACTIVITY_PATH = "com.lingshot.languagelearn.MainActivity"
        private const val SCREEN_CAPTURE_DATA = "SCREEN_CAPTURE_DATA"
        private const val STOP_SERVICE = "STOP_SERVICE"
        private const val NOTIFICATION_FOREGROUND_ID = 1
        var isScreenCaptureForSubtitle: Boolean = false

        fun screenShotServiceIntent(context: Context?): Intent {
            return Intent(context, ScreenShotService::class.java)
        }

        fun screenShotServiceIntentWithMediaProjection(context: Context?, data: Intent?): Intent {
            return Intent(context, ScreenShotService::class.java).apply {
                putExtra(SCREEN_CAPTURE_DATA, data)
            }
        }
    }
}
