package com.eb5.app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eb5.app.data.model.AppLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboarding_shows_language_picker() {
        composeTestRule.setContent {
            OnboardingScreen(
                language = AppLanguage.EN,
                availableLanguages = AppLanguage.supported,
                onLanguageChanged = {},
                onContinue = {},
                articles = emptyList()
            )
        }

        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    }
}
