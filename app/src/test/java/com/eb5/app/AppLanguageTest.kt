package com.eb5.app

import com.eb5.app.data.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `fromTag returns matching language`() {
        assertEquals(AppLanguage.ZH, AppLanguage.fromTag("zh"))
    }

    @Test
    fun `fromTag falls back to english`() {
        assertEquals(AppLanguage.EN, AppLanguage.fromTag("es"))
    }
}
