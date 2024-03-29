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
package com.lingshot.screenshot_presentation

import android.graphics.Bitmap
import com.lingshot.designsystem.component.ActionCropImage
import com.lingshot.screenshot_domain.model.ReadModeType

sealed class ScreenShotEvent {

    data object ClearStatus : ScreenShotEvent()

    data class ChangeReadMode(
        val readModeType: ReadModeType,
    ) : ScreenShotEvent()

    data class CroppedImage(
        val actionCropImage: ActionCropImage?,
    ) : ScreenShotEvent()

    data class FetchCorrectedOriginalText(
        val originalText: String,
    ) : ScreenShotEvent()

    data class FetchLanguageIdentifier(
        val originalText: String,
        val illegiblePhrase: String,
    ) : ScreenShotEvent()

    data class FetchTextRecognizer(
        val imageBitmap: Bitmap?,
    ) : ScreenShotEvent()

    data class ToggleDictionaryFullScreenDialog(
        val url: String?,
    ) : ScreenShotEvent()
}
