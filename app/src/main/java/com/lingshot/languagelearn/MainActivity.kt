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
package com.lingshot.languagelearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lingshot.designsystem.theme.LingshotTheme
import com.lingshot.languagelearn.presentation.MainViewModel
import com.lingshot.languagelearn.presentation.ui.MainRoute
import com.lingshot.screencapture.helper.ScreenCaptureFloatingWindowLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

//Lingshot Legacy
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var screenCaptureFloatingWindowLifecycle: ScreenCaptureFloatingWindowLifecycle
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        var isSignInSuccessful by mutableStateOf(false)

        lifecycleScope.launch {
            viewModel.isSignInSuccessful
                .onEach {
                    isSignInSuccessful = it
                }
                .collect()
        }

        installSplashScreen().apply {
            if (isSignInSuccessful) {
                setKeepOnScreenCondition { false }
            }
        }

        setContent {
            LingshotTheme {
                LingshotApp()
            }
        }
        screenCaptureFloatingWindowLifecycle(this)
    }

    @Composable
    private fun LingshotApp(
        systemUiController: SystemUiController = rememberSystemUiController(),
        statusBarColor: Color = MaterialTheme.colorScheme.surface,
    ) {
        SideEffect {
            systemUiController.setStatusBarColor(statusBarColor)
        }
        MainRoute(viewModel = viewModel)
    }
}
