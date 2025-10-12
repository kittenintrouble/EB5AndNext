package com.eb5.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eb5.app.R
import com.eb5.app.data.model.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: AppLanguage,
    availableLanguages: List<AppLanguage>,
    onLanguageChanged: (AppLanguage) -> Unit,
    onClose: () -> Unit,
    appVersion: String
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.settings_language_title))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = stringResource(language.displayName),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(R.string.label_language)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                availableLanguages.forEach { option ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = stringResource(option.displayName)) },
                        onClick = {
                            expanded = false
                            onLanguageChanged(option)
                        }
                    )
                }
            }
        }
        Text(text = stringResource(R.string.settings_version, appVersion))
        Button(onClick = onClose) {
            Text(text = stringResource(R.string.action_close))
        }
    }
}
