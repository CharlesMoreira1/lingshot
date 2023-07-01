package com.teachmeprint.screenshot_presentation

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.*
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeprint.common.helper.launchWithStatus
import com.teachmeprint.domain.PromptChatGPTConstant.PROMPT_CORRECT_SPELLING
import com.teachmeprint.domain.PromptChatGPTConstant.PROMPT_TRANSLATE
import com.teachmeprint.domain.model.ChatGPTPromptBodyDomain
import com.teachmeprint.domain.model.LanguageCodeFromAndToDomain
import com.teachmeprint.domain.model.PhraseDomain
import com.teachmeprint.domain.model.Status
import com.teachmeprint.domain.model.statusDefault
import com.teachmeprint.domain.model.statusEmpty
import com.teachmeprint.domain.model.statusError
import com.teachmeprint.domain.model.statusLoading
import com.teachmeprint.domain.model.statusSuccess
import com.teachmeprint.domain.repository.ChatGPTRepository
import com.teachmeprint.domain.repository.PhraseCollectionRepository
import com.teachmeprint.domain.usecase.SavePhraseLanguageUseCase
import com.teachmeprint.languagechoice_domain.model.AvailableLanguage
import com.teachmeprint.languagechoice_domain.repository.LanguageChoiceRepository
import com.teachmeprint.screenshot_domain.model.LanguageTranslationDomain
import com.teachmeprint.screenshot_domain.repository.ScreenShotRepository
import com.teachmeprint.screenshot_presentation.ui.component.ActionCropImage
import com.teachmeprint.screenshot_presentation.ui.component.ActionCropImage.CROPPED_IMAGE
import com.teachmeprint.screenshot_presentation.ui.component.ActionCropImage.FOCUS_IMAGE
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem.FOCUS
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem.LANGUAGE
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem.LISTEN
import com.teachmeprint.screenshot_presentation.ui.component.NavigationBarItem.TRANSLATE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScreenShotViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val chatGPTRepository: ChatGPTRepository,
    private val screenShotRepository: ScreenShotRepository,
    private val languageChoiceRepository: LanguageChoiceRepository,
    private val phraseCollectionRepository: PhraseCollectionRepository,
    private val savePhraseLanguageUseCase: SavePhraseLanguageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenShotUiState())
    val uiState = _uiState.asStateFlow()

    private val textToSpeech: TextToSpeech by lazy {
        TextToSpeech(context) { status ->
            if (status != SUCCESS) {
                _uiState.update {
                    it.copy(screenShotStatus = statusError(STATUS_TEXT_TO_SPEECH_FAILED))
                }
            }
        }
    }

    init {
        setupTextToSpeech()
    }

    fun handleEvent(screenShotEvent: ScreenShotEvent) {
        when (screenShotEvent) {
            is ScreenShotEvent.CroppedImage -> {
                croppedImage(screenShotEvent.actionCropImage)
            }

            is ScreenShotEvent.FetchCorrectedOriginalText -> {
                fetchCorrectedOriginalText(screenShotEvent.originalText)
            }

            is ScreenShotEvent.FetchTextRecognizer -> {
                fetchTextRecognizer(screenShotEvent.imageBitmap)
            }

            is ScreenShotEvent.SaveLanguage -> {
                saveLanguage(screenShotEvent.availableLanguage)
            }

            is ScreenShotEvent.SavePhraseInLanguageCollection -> {
                savePhraseInLanguageCollection(
                    screenShotEvent.originalText,
                    screenShotEvent.translatedText
                )
            }

            is ScreenShotEvent.SelectedOptionsLanguage -> {
                selectedOptionsLanguage(screenShotEvent.availableLanguage)
            }

            is ScreenShotEvent.SelectedOptionsNavigationBar -> {
                selectedOptionsNavigationBar(screenShotEvent.navigationBarItem)
            }

            is ScreenShotEvent.CheckPhraseInLanguageCollection -> {
                checkPhraseInLanguageCollection(screenShotEvent.originalText)
            }

            is ScreenShotEvent.ClearStatus -> {
                clearStatus()
            }

            is ScreenShotEvent.ToggleLanguageDialog -> {
                toggleLanguageDialog()
            }

            is ScreenShotEvent.ToggleLanguageDialogAndHideSelectionAlert -> {
                toggleLanguageDialogAndHideSelectionAlert()
            }
        }
    }

    private fun croppedImage(actionCropImageType: ActionCropImage?) {
        _uiState.update { it.copy(actionCropImage = actionCropImageType) }
    }

    private fun selectedOptionsLanguage(availableLanguage: AvailableLanguage?) {
        _uiState.update { it.copy(availableLanguage = availableLanguage) }
    }

    private fun selectedOptionsNavigationBar(navigationBarItem: NavigationBarItem) {
        when (navigationBarItem) {
            TRANSLATE -> {
                getLanguage()?.let {
                    croppedImage(CROPPED_IMAGE)
                } ?: run {
                    showLanguageSelectionAlert()
                }
            }

            LISTEN -> {
                croppedImage(CROPPED_IMAGE)
            }

            FOCUS -> {
                croppedImage(FOCUS_IMAGE)
            }

            LANGUAGE -> {
                toggleLanguageDialog()
            }
        }
        _uiState.update { it.copy(navigationBarItem = navigationBarItem) }
    }

    private fun showLanguageSelectionAlert() {
        _uiState.update {
            it.copy(
                isLanguageSelectionAlertVisible = !it.isLanguageSelectionAlertVisible
            )
        }
    }

    private fun toggleLanguageDialog() {
        _uiState.update {
            it.copy(
                isLanguageDialogVisible = !it.isLanguageDialogVisible,
                availableLanguage = getLanguage()
            )
        }
    }

    private fun toggleLanguageDialogAndHideSelectionAlert() {
        _uiState.update {
            it.copy(
                isLanguageDialogVisible = !it.isLanguageDialogVisible,
                isLanguageSelectionAlertVisible = !it.isLanguageSelectionAlertVisible
            )
        }
    }

    private fun clearStatus() {
        _uiState.update {
            it.copy(
                screenShotStatus = statusDefault(),
                correctedOriginalTextStatus = statusDefault(),
                isPhraseSaved = false
            )
        }
    }

    private fun fetchTextRecognizer(imageBitmap: Bitmap?) {
        viewModelScope.launch {
            when (val status = screenShotRepository.fetchTextRecognizer(imageBitmap)) {
                is Status.Success -> {
                    val textFormatted = status.data.formatText()
                    when (_uiState.value.navigationBarItem) {
                        TRANSLATE -> fetchPhraseToTranslate(textFormatted)
                        LISTEN -> fetchLanguageIdentifier(textFormatted)
                        else -> Unit
                    }
                }

                is Status.Error -> {
                    _uiState.update { value ->
                        value.copy(
                            screenShotStatus = statusError(status.statusMessage)
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    private fun fetchPhraseToTranslate(text: String) {
        if (text.isNotBlank()) {
            viewModelScope.launchWithStatus({
                val requestBody = ChatGPTPromptBodyDomain(
                    prompt = PROMPT_TRANSLATE(getLanguage()?.displayName, text)
                )
                LanguageTranslationDomain(
                    originalText = text,
                    translatedText = chatGPTRepository.get(requestBody),
                    languageCodeFromAndTo = text.getLanguageCodeFromAndToDomain().name
                )
            }, { status ->
                _uiState.update { it.copy(screenShotStatus = status) }
            })
        } else {
            _uiState.update { it.copy(screenShotStatus = statusEmpty()) }
        }
    }

    private fun fetchCorrectedOriginalText(originalText: String) {
        viewModelScope.launchWithStatus({
            val requestBody = ChatGPTPromptBodyDomain(
                prompt = PROMPT_CORRECT_SPELLING(originalText)
            )
            chatGPTRepository.get(requestBody)
        }, { status ->
            _uiState.update { it.copy(correctedOriginalTextStatus = status) }
        })
    }

    private fun fetchLanguageIdentifier(text: String) {
        viewModelScope.launch {
            when (val status = screenShotRepository.fetchLanguageIdentifier(text)) {
                is Status.Success -> {
                    fetchTextToSpeech(text.ifBlank { ILLEGIBLE_TEXT }, status.data.toString())
                }

                is Status.Error -> {
                    _uiState.update { value ->
                        value.copy(
                            screenShotStatus = statusError(status.statusMessage)
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    private fun fetchTextToSpeech(text: String, languageCode: String) =
        with(textToSpeech) {
            val languageLocale = if (languageCode == LANGUAGE_CODE_UNAVAILABLE) {
                Locale.US
            } else {
                Locale.forLanguageTag(languageCode)
            }

            val result = setLanguage(languageLocale)
            if (result == LANG_MISSING_DATA || result == LANG_NOT_SUPPORTED) {
                _uiState.update { value ->
                    value.copy(
                        screenShotStatus = statusError(
                            STATUS_TEXT_TO_SPEECH_NOT_SUPPORTED
                        )
                    )
                }
            }

            speak(text, QUEUE_FLUSH, null, "")
        }

    private fun setupTextToSpeech() =
        with(textToSpeech) {
            setOnUtteranceProgressListener(onSpeechListener())
            setSpeechRate(0.7f)
        }

    private fun onSpeechListener() = object : UtteranceProgressListener() {
        override fun onStart(p0: String?) {
            _uiState.update { it.copy(screenShotStatus = statusLoading()) }
        }

        override fun onDone(value: String?) {
            _uiState.update { it.copy(screenShotStatus = statusSuccess(null)) }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(p0: String?) {
            _uiState.update {
                it.copy(screenShotStatus = statusError(p0))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun getLanguage(): AvailableLanguage? {
        return languageChoiceRepository.getLanguage()
    }

    private fun saveLanguage(availableLanguage: AvailableLanguage?) {
        viewModelScope.launch {
            languageChoiceRepository.saveLanguage(availableLanguage)
        }
    }

    private fun savePhraseInLanguageCollection(originalText: String, translatedText: String) {
        viewModelScope.launch {
            val languageDomain = originalText.getLanguageCodeFromAndToDomain()
            val phraseDomain = PhraseDomain(original = originalText, translate = translatedText)
            _uiState.update {
                it.copy(
                    isPhraseSaved = savePhraseLanguageUseCase(
                        languageDomain,
                        phraseDomain
                    )
                )
            }
        }
    }

    private fun checkPhraseInLanguageCollection(originalText: String) {
        viewModelScope.launch {
            val languageDomain = originalText.getLanguageCodeFromAndToDomain()
            _uiState.update {
                it.copy(
                    isPhraseSaved = phraseCollectionRepository.isPhraseSaved(
                        languageDomain.name,
                        originalText
                    )
                )
            }
        }
    }

    private suspend fun String.getLanguageCodeFromAndToDomain(): LanguageCodeFromAndToDomain {
        val status = screenShotRepository.fetchLanguageIdentifier(this)
        if (status is Status.Success) {
            return LanguageCodeFromAndToDomain(
                name = "${status.data}_${getLanguage()?.languageCode}"
            )
        }
        return LanguageCodeFromAndToDomain()
    }

    private fun String?.formatText(): String {
        return toString()
            .replace("\n", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    companion object {
        const val ILLEGIBLE_TEXT = "There isn't any legible text."
        private const val LANGUAGE_CODE_UNAVAILABLE = "und"
        private const val STATUS_TEXT_TO_SPEECH_FAILED = "Text to speech failed."
        private const val STATUS_TEXT_TO_SPEECH_NOT_SUPPORTED = "Text to speech not supported."
    }
}