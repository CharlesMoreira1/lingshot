@file:OptIn(ExperimentalMaterial3Api::class)

package com.lingshot.phrasemaster_presentation.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lingshot.designsystem.component.LingshotFullScreenDialog
import com.lingshot.phrasemaster_presentation.R
import com.phrase.phrasemaster_domain.model.PhraseDomain
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun EditPhraseFullScreenDialog(
    phraseState: PhraseState,
    onSavePhraseInLanguageCollection: (PhraseDomain) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(phraseState) }

    LingshotFullScreenDialog(
        modifier = modifier,
        title = stringResource(R.string.text_title_toolbar_edit_phrase),
        onDismiss = onDismiss,
        onActions = {
            TextButton(onClick = {
                state.updateBracketsStatus()

                if (state.hasWordInDoubleSquareBrackets) {
                    onSavePhraseInLanguageCollection(state.phraseDomain)
                }
            }) {
                Text(text = stringResource(R.string.text_button_save_edit_phrase))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.phraseDomain.original,
                onValueChange = { newValue ->
                    state = state.copy(
                        phraseDomain = state.phraseDomain.copy(original = newValue)
                    )

                    state.updateBracketsStatus()
                },
                label = {
                    Text(
                        text = stringResource(R.string.text_label_original_input_edit_phrase)
                    )
                }
            )
            MarkdownText(
                markdown = stringResource(R.string.text_markdown_enclose_word_edit_phrase)
            )
            AnimatedVisibility(visible = state.hasWordInDoubleSquareBrackets.not()) {
                MarkdownText(
                    modifier = Modifier.padding(top = 4.dp),
                    markdown = stringResource(R.string.text_markdown_alert_enclose_word_edit_phrase)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.phraseDomain.translate,
                onValueChange = { newValue ->
                    state = state.copy(
                        phraseDomain = state.phraseDomain.copy(translate = newValue)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.text_label_translate_input_edit_phrase)
                    )
                }
            )
        }
    }

    if (phraseState.isValidLanguage.not()) {
        Toast.makeText(
            context,
            stringResource(R.string.text_message_invalid_language),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Preview(showBackground = true)
@Composable
private fun EditPhraseFullScreenDialogPreview() {
    EditPhraseFullScreenDialog(
        onDismiss = {},
        phraseState = PhraseState(
            PhraseDomain(
                "What's your name?",
                "Qual seu nome?"
            )
        ),
        onSavePhraseInLanguageCollection = {}
    )
}

@Immutable
data class PhraseState(
    val phraseDomain: PhraseDomain = PhraseDomain(),
    val isValidLanguage: Boolean = true
) {
    var hasWordInDoubleSquareBrackets by mutableStateOf(true)
        private set

    fun updateBracketsStatus() {
        val regex = Regex("\\[\\[([^\\[\\]\\s]+)]]")
        val matches = regex.findAll(phraseDomain.original)
        val words = matches.map { it.groupValues[1] }.toList()

        hasWordInDoubleSquareBrackets = (words.size == 1)
    }
}
