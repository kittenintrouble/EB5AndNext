package com.eb5.app.data.model

import androidx.annotation.StringRes
import com.eb5.app.R
import java.util.Locale

enum class AppLanguage(
    val tag: String,
    val assetFolder: String,
    @StringRes val displayName: Int,
    @StringRes val nativeName: Int
) {
    EN("en", "en", R.string.language_english, R.string.language_native_english),
    ZH("zh", "zh", R.string.language_chinese, R.string.language_native_chinese),
    VI("vi", "vi", R.string.language_vietnamese, R.string.language_native_vietnamese),
    KO("ko", "ko", R.string.language_korean, R.string.language_native_korean);

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
