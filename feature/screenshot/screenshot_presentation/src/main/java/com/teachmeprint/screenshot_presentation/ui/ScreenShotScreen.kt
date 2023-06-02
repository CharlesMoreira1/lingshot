package com.teachmeprint.screenshot_presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeprint.common.helper.isLoadingStatus
import com.teachmeprint.common.helper.onError
import com.teachmeprint.common.helper.onLoading
import com.teachmeprint.common.helper.onSuccess
import com.teachmeprint.designsystem.theme.TeachMePrintTheme
import com.teachmeprint.languagechoice_presentation.ui.LanguageChoiceDialog
import com.teachmeprint.screenshot_presentation.R
import com.teachmeprint.screenshot_presentation.ScreenShotEvent
import com.teachmeprint.screenshot_presentation.ScreenShotEvent.*
import com.teachmeprint.screenshot_presentation.ScreenShotUiState
import com.teachmeprint.screenshot_presentation.ScreenShotViewModel
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem.TRANSLATE
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotCropImage
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotLottieLoading
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotNavigationBar
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotNavigationBarItem
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotSnackBarError
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotSnackBarSelectLanguage
import com.teachmeprint.screenshot_presentation.ui.component.ScreenShotTranslateBottomSheet
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ScreenShotRoute(
    viewModel: ScreenShotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeachMePrintTheme {
        ScreenShotScreen(
            uiState = uiState,
            handleEvent = viewModel::handleEvent
        )
    }
}

@Composable
private fun ScreenShotScreen(
    uiState: ScreenShotUiState,
    modifier: Modifier = Modifier,
    handleEvent: (event: ScreenShotEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ScreenShotCropImage(
            actionCropImage = uiState.actionCropImage,
            onCropImageResult = { bitmap ->
                handleEvent(FetchTextRecognizer(bitmap))
            },
            onCroppedImage = {
                handleEvent(CroppedImage(it))
            }
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            uiState.screenShotStatus.onLoading {
                val loading = uiState.navigationBarItem
                    .takeIf { it == TRANSLATE }
                    ?.let { R.raw.loading_translate } ?: R.raw.loading_listen

                ScreenShotLottieLoading(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    loading = loading
                )
            }.onSuccess {
                if (uiState.navigationBarItem == TRANSLATE) {
                    ScreenShotTranslateBottomSheet(
                        text = it,
                        onDismiss = {
                            handleEvent(ClearStatus)
                        }
                    )
                }
            }.onError {
                ScreenShotSnackBarError(
                    modifier = Modifier.padding(bottom = 16.dp),
                    code = it,
                    onDismiss = {
                        handleEvent(ClearStatus)
                    }
                )
            }
            if (uiState.isLanguageSelectionAlertVisible) {
                ScreenShotSnackBarSelectLanguage(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onToggleLanguageDialogAndHideSelectionAlert = {
                        handleEvent(ToggleLanguageDialogAndHideSelectionAlert)
                    }
                )
            }
            ScreenShotNavigationBar(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                ScreenShotNavigationBarItem(
                    navigationBarItem = uiState.navigationBarItem,
                    navigationBarItemList = uiState.navigationBarItemList,
                    onSelectedOptionsNavigationBar = { item ->
                        if (!uiState.screenShotStatus.isLoadingStatus) {
                            handleEvent(SelectedOptionsNavigationBar(item))
                        }
                    }
                )
            }
        }
    }
    if (uiState.isLanguageDialogVisible) {
        LanguageChoiceDialog(
            availableLanguage = uiState.availableLanguage,
            availableLanguageList = uiState.availableLanguageList.toImmutableList(),
            onSaveLanguage = {
                handleEvent(SaveLanguage(it))
            },
            onSelectedOptionsLanguage = {
                handleEvent(SelectedOptionsLanguage(it))
            },
            onDismiss = {
                handleEvent(ToggleLanguageDialog)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenShotScreenPreview() {
    ScreenShotScreen(
        uiState = ScreenShotUiState(),
        handleEvent = {}
    )
}