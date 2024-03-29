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
package com.lingshot.languagechoice_data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lingshot.languagechoice_domain.model.AvailableLanguage
import com.lingshot.languagechoice_domain.model.TranslateLanguageType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LanguageChoiceLocalStorage @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = LANGUAGE_DATA,
    )

    fun getLanguage(translateLanguageType: TranslateLanguageType): Flow<AvailableLanguage?> {
        return context.dataStore.data.map { preferences ->
            val languageCode =
                if (translateLanguageType == TranslateLanguageType.FROM) {
                    preferences[LANGUAGE_FROM_CODE_KEY] ?: AvailableLanguage.ENGLISH.languageCode
                } else {
                    preferences[LANGUAGE_TO_CODE_KEY] ?: AvailableLanguage.ENGLISH.languageCode
                }
            AvailableLanguage.from(languageCode)
        }
    }

    suspend fun saveLanguage(availableLanguage: AvailableLanguage?, translateLanguageType: TranslateLanguageType?) {
        context.dataStore.edit { preferences ->
            if (translateLanguageType == TranslateLanguageType.FROM) {
                preferences[LANGUAGE_FROM_CODE_KEY] = availableLanguage?.languageCode.toString()
            } else {
                preferences[LANGUAGE_TO_CODE_KEY] = availableLanguage?.languageCode.toString()
            }
        }
    }

    companion object {
        private const val LANGUAGE_DATA: String = "LANGUAGE_DATA"
        private val LANGUAGE_FROM_CODE_KEY = stringPreferencesKey("LANGUAGE_FROM_CODE_KEY")
        private val LANGUAGE_TO_CODE_KEY = stringPreferencesKey("LANGUAGE_TO_CODE_KEY")
    }
}
