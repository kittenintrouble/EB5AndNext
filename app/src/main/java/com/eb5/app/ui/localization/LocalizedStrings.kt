package com.eb5.app.ui.localization

import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.eb5.app.ui.theme.LocalAppLanguage
import java.util.Locale

@Composable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any?): String {
    val context = LocalContext.current
    val language = LocalAppLanguage.current

    val resources = remember(context, language) {
        val configuration = Configuration(context.resources.configuration)
        val locale = Locale.forLanguageTag(language.tag)
        configuration.setLocales(LocaleList(locale))
        context.createConfigurationContext(configuration).resources
    }

    return if (formatArgs.isNotEmpty()) {
        resources.getString(id, *formatArgs)
    } else {
        resources.getString(id)
    }
}
