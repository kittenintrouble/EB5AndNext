package com.eb5.app.data.repositories

import android.util.Log
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.data.network.NewsApiService

class NewsRepository(
    private val api: NewsApiService
) {

    suspend fun news(language: AppLanguage, limit: Int = 20, offset: Int = 0): List<NewsArticle> {
        return runCatching {
            api.getNews(language.tag, limit = limit, offset = offset)
                .items
                .filter { it.published }
                .sortedByDescending { it.publishedAt }
        }.getOrElse { error ->
            Log.w(TAG, "Failed to load news for ${language.tag}", error)
            emptyList()
        }
    }

    suspend fun article(articleId: String, language: AppLanguage): NewsArticle? {
        return runCatching {
            api.getArticleById(articleId, language.tag)
        }.getOrElse { error ->
            Log.w(TAG, "Failed to load article $articleId for ${language.tag}", error)
            null
        }
    }

    suspend fun latest(language: AppLanguage): NewsArticle? =
        runCatching { api.getLatestArticle(language.tag) }
            .getOrNull()
            ?.takeIf { it.published }

    companion object {
        private const val TAG = "NewsRepository"
    }
}
