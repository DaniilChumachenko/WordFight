package com.chvma.wordfight.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.localization.AppStrings

@Composable
fun PlayerOnboardingDialog(
    name: String,
    selectedLanguage: AppLanguage,
    onNameChange: (String) -> Unit,
    onLanguageSelect: (AppLanguage) -> Unit,
    onConfirm: () -> Unit,
    strings: AppStrings,
) {
    val canConfirm = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = strings.onboardingTitle,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { value ->
                        onNameChange(value.take(24))
                    },
                    singleLine = true,
                    label = { Text(strings.onboardingNameLabel) },
                    placeholder = { Text(strings.onboardingNamePlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = strings.onboardingLanguageLabel,
                    style = MaterialTheme.typography.titleSmall,
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    AppLanguage.supported.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageSelect(language) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = { onLanguageSelect(language) },
                            )
                            Text(text = language.nativeName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canConfirm,
            ) {
                Text(strings.onboardingConfirm)
            }
        },
    )
}
