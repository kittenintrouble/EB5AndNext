package com.eb5.app.data.repositories

import android.content.res.AssetManager
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Article
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class ContentRepository(private val assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, List<Article>>()
    private val mutex = Mutex()

    suspend fun articles(language: AppLanguage): List<Article> = mutex.withLock {
        cache.getOrPut(language.assetFolder) {
            runCatching { loadArticles(language.assetFolder) }
                .getOrElse { error ->
                    if (language != AppLanguage.EN) {
                        runCatching { loadArticles(AppLanguage.EN.assetFolder) }
                            .onFailure { error.printStackTrace() }
                            .getOrDefault(emptyList())
                    } else {
                        error.printStackTrace()
                        emptyList()
                    }
                }
        }
    }

    private fun loadArticles(folder: String): List<Article> {
        val path = "content/$folder/eb5_terms.json"
        return assets.open(path).bufferedReader().use { reader ->
            json.decodeFromString<List<Article>>(reader.readText())
        }
    }
}
