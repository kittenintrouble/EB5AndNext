package com.eb5.app.data.model

import com.google.gson.annotations.SerializedName

data class NewsArticle(
    @SerializedName("id") val id: String,
    @SerializedName("slug") val slug: String?,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("category") val category: String?,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("title") val title: String,
    @SerializedName("short_description") val shortDescription: String?,
    @SerializedName("hero_image") val heroImage: HeroImage?,
    @SerializedName("article") val article: List<ArticleBlock> = emptyList(),
    @SerializedName("related_ids") val relatedIds: List<String> = emptyList(),
    @SerializedName("meta") val meta: ArticleMeta? = null,
    @SerializedName("published") val published: Boolean = true
)

data class HeroImage(
    @SerializedName("url") val url: String,
    @SerializedName("alt") val alt: String?,
    @SerializedName("caption") val caption: String?,
    @SerializedName("credit") val credit: String?
)

data class ArticleBlock(
    @SerializedName("type") val type: String,
    // Heading
    @SerializedName("level") val level: Int? = null,
    @SerializedName("text") val text: String? = null,
    // Image
    @SerializedName("url") val url: String? = null,
    @SerializedName("alt") val alt: String? = null,
    @SerializedName("caption") val caption: String? = null,
    // Quote
    @SerializedName("quote") val quote: String? = null,
    @SerializedName("author") val author: String? = null,
    // Table
    @SerializedName("headers") val headers: List<String>? = null,
    @SerializedName("rows") val rows: List<List<String>>? = null,
    // List
    @SerializedName("items") val items: List<String>? = null,
    // Link
    @SerializedName("href") val href: String? = null
)

data class ArticleMeta(
    @SerializedName("author") val author: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("tags") val tags: List<String>?
)
