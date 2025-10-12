package com.eb5.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.eb5.app.data.model.AppLanguage

val LocalizedTheme = mapOf(
    AppLanguage.EN to ThemeVariant(
        colorScheme = lightColorScheme(
            primary = Color(0xFF005B96),
            onPrimary = Color.White,
            secondary = Color(0xFF0B7FAB),
            onSecondary = Color.White,
            tertiary = Color(0xFF2E8B57),
            background = Color(0xFFF1F5F9),
            surface = Color.White
        )
    ),
    AppLanguage.ZH to ThemeVariant(
        colorScheme = lightColorScheme(
            primary = Color(0xFFB71C1C),
            onPrimary = Color.White,
            secondary = Color(0xFFD84315),
            onSecondary = Color.White,
            tertiary = Color(0xFF8D6E63),
            background = Color(0xFFFFF3E0),
            surface = Color.White
        )
    ),
    AppLanguage.VI to ThemeVariant(
        colorScheme = lightColorScheme(
            primary = Color(0xFFF57F17),
            onPrimary = Color.White,
            secondary = Color(0xFFAD1457),
            onSecondary = Color.White,
            tertiary = Color(0xFF4E342E),
            background = Color(0xFFFFF8E1),
            surface = Color.White
        )
    ),
    AppLanguage.KO to ThemeVariant(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1565C0),
            onPrimary = Color.White,
            secondary = Color(0xFF00695C),
            onSecondary = Color.White,
            tertiary = Color(0xFF424242),
            background = Color(0xFFE8F5E9),
            surface = Color.White
        )
    )
)

fun themeFor(language: AppLanguage): ThemeVariant = LocalizedTheme[language] ?: LocalizedTheme.getValue(AppLanguage.EN)

data class ThemeVariant(
    val colorScheme: ColorScheme
)
