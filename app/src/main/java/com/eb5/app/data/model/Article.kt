package com.eb5.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: Int,
    val title: String,
    val category: String,
    val subcategory: String,
    val description: String,
    val shortDescription: String? = null,
    val examples: List<String> = emptyList(),
    val dayNumber: Int? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class ArticleCollection(
    val items: List<Article>
)
