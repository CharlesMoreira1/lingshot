package com.teachmeprint.language.feature.screenshot.repository

import com.teachmeprint.language.data.local.storage.LanguageLocalStorage
import com.teachmeprint.language.data.local.storage.TranslationCountLocalStore
import com.teachmeprint.language.data.model.language.AvailableLanguage
import com.teachmeprint.language.data.model.screenshot.entity.RequestBody
import com.teachmeprint.language.data.model.screenshot.entity.TranslateChatGPTResponse
import com.teachmeprint.language.data.remote.api.TranslateChatGPTService
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ScreenShotRepository @Inject constructor(
    private val translateChatGPTService: TranslateChatGPTService,
    private val languageLocalStorage: LanguageLocalStorage,
    private val translationCountLocalStore: TranslationCountLocalStore
) {

    suspend fun getTranslatePhrase(message: RequestBody): TranslateChatGPTResponse {
        return translateChatGPTService.getTranslatePhrase(message)
    }

    fun getLanguage(): AvailableLanguage? {
        return languageLocalStorage.getLanguage()
    }

    fun saveLanguage(availableLanguage: AvailableLanguage?) {
        languageLocalStorage.saveLanguage(availableLanguage)
    }

    fun saveTranslationCount() {
        translationCountLocalStore.saveTranslationCount()
    }

    fun hasReachedMaxTranslationCount(): Boolean {
        return translationCountLocalStore.hasReachedMaxTranslationCount()
    }
}