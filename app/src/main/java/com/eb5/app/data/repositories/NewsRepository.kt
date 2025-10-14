package com.eb5.app.data.repositories

import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.data.network.NewsApiService

class NewsRepository(
    private val api: NewsApiService
) {

    suspend fun news(language: AppLanguage, limit: Int = 20, offset: Int = 0): List<NewsArticle> {
        return api.getNews(language.tag, limit = limit, offset = offset)
            .items
            .filter { it.published }
            .sortedByDescending { it.publishedAt }
    }

    suspend fun article(articleId: String, language: AppLanguage): NewsArticle {
        return api.getArticleById(articleId, language.tag)
    }

    suspend fun latest(language: AppLanguage): NewsArticle? =
        runCatching { api.getLatestArticle(language.tag) }
            .getOrNull()
            ?.takeIf { it.published }
}
