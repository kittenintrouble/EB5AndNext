package com.eb5.app.data.network

import com.eb5.app.BuildConfig
import com.eb5.app.data.model.Project
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ProjectsResponse(
    @SerializedName("projects")
    val projects: List<Project>? = null,
    @SerializedName("items")
    val items: List<Project>? = null,
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("limit")
    val limit: Int = 0,
    @SerializedName("offset")
    val offset: Int = 0
) {
    val all: List<Project>
        get() = when {
            !projects.isNullOrEmpty() -> projects
            !items.isNullOrEmpty() -> items
            else -> emptyList()
        }
}

interface ProjectsApiService {

    @GET("projects")
    suspend fun getProjects(
        @Query("lang") language: String,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("published") published: Boolean = true,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ProjectsResponse

    @GET("projects/{id}")
    suspend fun getProject(
        @Path("id") projectId: String,
        @Query("lang") language: String
    ): Project
}

fun createProjectsApiService(baseUrl: String = BuildConfig.NEWS_BASE_URL): ProjectsApiService {
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

    return retrofit.create(ProjectsApiService::class.java)
}
