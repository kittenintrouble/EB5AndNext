package com.eb5.app.data.model

import androidx.annotation.StringRes
import com.eb5.app.R
import java.util.Locale

enum class AppLanguage(val tag: String, val assetFolder: String, @StringRes val displayName: Int) {
    EN("en", "en", R.string.language_english),
    ZH("zh", "zh", R.string.language_chinese),
    VI("vi", "vi", R.string.language_vietnamese),
    KO("ko", "ko", R.string.language_korean);

    companion object {
        val supported = entries

        fun fromTag(tag: String?): AppLanguage {
            if (tag == null) {
                val systemTag = Locale.getDefault().language
                return entries.firstOrNull { it.tag == systemTag } ?: EN
            }
            return entries.firstOrNull { it.tag == tag } ?: EN
        }
    }
}
