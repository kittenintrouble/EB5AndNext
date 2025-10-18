package com.eb5.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.eb5.app.data.model.AppLanguage

val LocalThemeVariant = staticCompositionLocalOf { themeFor(AppLanguage.EN) }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }

@Composable
fun EB5Theme(language: AppLanguage, content: @Composable () -> Unit) {
    val themeVariant = themeFor(language)
    CompositionLocalProvider(
        LocalThemeVariant provides themeVariant,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = themeVariant.colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
