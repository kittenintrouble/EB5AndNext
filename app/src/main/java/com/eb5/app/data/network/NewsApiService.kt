package com.eb5.app.data.network

import com.eb5.app.BuildConfig
import com.eb5.app.data.model.NewsArticle
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class NewsListResponse(
    val items: List<NewsArticle> = emptyList(),
    @Suppress("PropertyName")
    val next_offset: String? = null
)

interface NewsApiService {

    @GET("news")
    suspend fun getNews(
        @Query("lang") language: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): NewsListResponse

    @GET("news/{id}")
    suspend fun getArticleById(
        @Path("id") articleId: String,
        @Query("lang") language: String
    ): NewsArticle

    @GET("news/latest")
    suspend fun getLatestArticle(
        @Query("lang") language: String
    ): NewsArticle
}

fun createNewsApiService(baseUrl: String = BuildConfig.NEWS_BASE_URL): NewsApiService {
    val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(NewsApiService::class.java)
}
