package com.eb5.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eb5.app.BuildConfig
import com.eb5.app.data.preferences.UserPreferencesRepository
import com.eb5.app.data.repositories.ContentRepository
import com.eb5.app.data.repositories.NewsRepository
import com.eb5.app.data.repositories.ProjectRepository
import com.eb5.app.data.repositories.QuizRepository
import com.eb5.app.data.network.createNewsApiService
import com.eb5.app.data.network.createProjectsApiService

class AppContainer(
    context: Context,
    dataStore: DataStore<Preferences>
) {
    private val appContext = context.applicationContext

    val userPreferencesRepository = UserPreferencesRepository(dataStore)
    val contentRepository = ContentRepository(appContext.assets)
    val quizRepository = QuizRepository(appContext.assets)
    private val projectsApiService = createProjectsApiService(BuildConfig.PROJECTS_BASE_URL)
    val projectRepository = ProjectRepository(projectsApiService)
    private val newsApiService = createNewsApiService(BuildConfig.NEWS_BASE_URL)
    val newsRepository = NewsRepository(newsApiService)
}
