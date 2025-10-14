package com.eb5.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eb5.app.BuildConfig
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.preferences.UserPreferencesRepository
import com.eb5.app.data.repositories.ContentRepository
import com.eb5.app.data.repositories.ProjectRepository
import com.eb5.app.data.repositories.QuizRepository
import com.eb5.app.data.repositories.NewsRepository
import com.eb5.app.ui.navigation.AppDestination
import com.eb5.app.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val contentRepository: ContentRepository,
    private val quizRepository: QuizRepository,
    private val projectRepository: ProjectRepository,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    data class BaseReturnSnapshot(
        val category: String?,
        val subcategory: String?,
        val index: Int,
        val offset: Int,
        val pending: Boolean = true
    )

    init {
        viewModelScope.launch {
            preferencesRepository.snapshot.collect { snapshot ->
                val previousState = _uiState.value
                _uiState.update { it.copy(error = null) }
                val language = snapshot.language
                LocaleManager.apply(language)

                val errors = mutableSetOf<String>()

                val articles = withContext(Dispatchers.IO) {
                    runCatching { contentRepository.articles(language) }
                }.getOrElse {
                    errors += "Article content is unavailable right now."
                    previousState.articles
                }

                val quizzes = withContext(Dispatchers.IO) {
                    runCatching { quizRepository.quizzes(language) }
                }.getOrElse {
                    errors += "Quizzes could not be loaded."
                    previousState.quizzes
                }

                val projects = withContext(Dispatchers.IO) {
                    runCatching { projectRepository.projects(language, forceRefresh = true) }
                }.getOrElse {
                    errors += "Projects could not be loaded."
                    previousState.projects
                }

                val news = withContext(Dispatchers.IO) {
                    runCatching { newsRepository.news(language) }
                }.getOrElse {
                    errors += "Latest news couldn't be refreshed."
                    previousState.news
                }

                _uiState.update {
                    it.copy(
                        language = language,
                        onboardingCompleted = snapshot.onboardingCompleted,
                        articles = articles,
                        quizzes = quizzes,
                        projects = projects,
                        favorites = snapshot.favorites,
                        articleStatuses = snapshot.articleStatuses,
                        quizProgress = snapshot.quizProgress,
                        news = news,
                        isLoading = false,
                        error = errors.joinToString("\n").takeIf { message -> message.isNotBlank() },
                        pendingScrollArticleId = it.pendingScrollArticleId,
                        baseResetToken = it.baseResetToken,
                        currentRoute = it.currentRoute,
                        pendingRestoreRoute = it.pendingRestoreRoute,
                        newsFavorites = snapshot.newsFavorites,
                        projectFavorites = snapshot.projectFavorites,
                        baseReturn = it.baseReturn,
                        pendingNewsArticleId = it.pendingNewsArticleId,
                        pendingNewsLanguage = it.pendingNewsLanguage,
                        pendingNewsReturnRoute = it.pendingNewsReturnRoute,
                        pendingProjectId = it.pendingProjectId,
                        pendingProjectLanguage = it.pendingProjectLanguage,
                        pendingProjectReturnRoute = it.pendingProjectReturnRoute
                    )
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        val currentRoute = _uiState.value.currentRoute
        if (currentRoute?.startsWith(AppDestination.Settings.route) == true) {
            _uiState.update { it.copy(pendingRestoreRoute = currentRoute) }
        }
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }

    fun completeOnboarding(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun toggleFavorite(articleId: Int) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(articleId)
        }
    }

    fun toggleArticleStatus(articleId: Int) {
        val current = _uiState.value.articleStatuses[articleId]
        val next = if (current == ArticleStatus.COMPLETED) ArticleStatus.IN_PROGRESS else ArticleStatus.COMPLETED
        viewModelScope.launch {
            preferencesRepository.setArticleStatus(articleId, next)
        }
    }

    fun setArticleInProgress(articleId: Int) {
        viewModelScope.launch {
            preferencesRepository.setArticleStatus(articleId, ArticleStatus.IN_PROGRESS)
        }
    }

    fun recordQuizResult(quizId: String, score: Int) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            preferencesRepository.updateQuizResult(quizId, score, timestamp)
        }
    }

    fun focusArticleOnReturn(articleId: Int) {
        _uiState.update { it.copy(pendingScrollArticleId = articleId) }
    }

    fun clearPendingArticleFocus() {
        _uiState.update { it.copy(pendingScrollArticleId = null) }
    }

    fun resetBaseScreen() {
        _uiState.update {
            it.copy(
                pendingScrollArticleId = null,
                baseResetToken = it.baseResetToken + 1
            )
        }
    }

    fun refreshNews() {
        val language = _uiState.value.language
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { newsRepository.news(language) }
            }
            result.onSuccess { articles ->
                _uiState.update { state ->
                    state.copy(news = articles)
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    val message = throwable.message ?: "Latest news couldn't be refreshed."
                    val combined = listOfNotNull(state.error, message)
                        .joinToString("\n")
                        .takeIf { it.isNotBlank() }
                    state.copy(error = combined)
                }
            }
        }
    }

    fun refreshProjects(force: Boolean = true) {
        val language = _uiState.value.language
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (force) {
                        projectRepository.refresh(language)
                    } else {
                        projectRepository.projects(language)
                    }
                }
            }
            result.onSuccess { projects ->
                _uiState.update { state ->
                    state.copy(projects = projects)
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    val message = throwable.message ?: "Projects could not be refreshed."
                    val combined = listOfNotNull(state.error, message)
                        .joinToString("\n")
                        .takeIf { it.isNotBlank() }
                    state.copy(error = combined)
                }
            }
        }
    }

    fun toggleNewsFavorite(newsId: String) {
        _uiState.update { state ->
            val updated = if (state.newsFavorites.contains(newsId)) {
                state.newsFavorites - newsId
            } else {
                state.newsFavorites + newsId
            }
            state.copy(newsFavorites = updated)
        }
        viewModelScope.launch {
            preferencesRepository.toggleNewsFavorite(newsId)
        }
    }

    fun toggleProjectFavorite(projectId: String) {
        _uiState.update { state ->
            val updated = if (state.projectFavorites.contains(projectId)) {
                state.projectFavorites - projectId
            } else {
                state.projectFavorites + projectId
            }
            state.copy(projectFavorites = updated)
        }
        viewModelScope.launch {
            preferencesRepository.toggleProjectFavorite(projectId)
        }
    }

    fun recordBaseReturn(category: String?, subcategory: String?, index: Int, offset: Int) {
        _uiState.update {
            it.copy(baseReturn = BaseReturnSnapshot(category, subcategory, index, offset, pending = true))
        }
    }

    fun activateBaseReturn() {
        _uiState.update { state ->
            val snapshot = state.baseReturn ?: return@update state
            state.copy(baseReturn = snapshot.copy(pending = false))
        }
    }

    fun clearBaseReturn() {
        _uiState.update { it.copy(baseReturn = null) }
    }

    fun updateCurrentDestination(route: String?) {
        _uiState.update { it.copy(currentRoute = route) }
    }

    fun clearPendingRestoreRoute() {
        _uiState.update { it.copy(pendingRestoreRoute = null) }
    }

    fun openNewsArticle(articleId: String, languageCode: String?, returnRoute: String? = null) {
        val fallbackRoute = _uiState.value.currentRoute ?: returnRoute
        _uiState.update {
            it.copy(
                pendingNewsArticleId = articleId,
                pendingNewsLanguage = languageCode,
                pendingNewsReturnRoute = fallbackRoute
            )
        }
    }

    fun openProject(projectId: String, languageCode: String?, returnRoute: String? = null) {
        val fallbackRoute = _uiState.value.currentRoute ?: returnRoute
        _uiState.update {
            it.copy(
                pendingProjectId = projectId,
                pendingProjectLanguage = languageCode,
                pendingProjectReturnRoute = fallbackRoute
            )
        }
    }

    fun clearPendingNewsArticle() {
        _uiState.update { it.copy(pendingNewsArticleId = null, pendingNewsLanguage = null) }
    }

    fun clearPendingNewsReturnRoute() {
        _uiState.update { it.copy(pendingNewsReturnRoute = null) }
    }

    fun clearPendingProject() {
        _uiState.update { it.copy(pendingProjectId = null, pendingProjectLanguage = null) }
    }

    fun clearPendingProjectReturnRoute() {
        _uiState.update { it.copy(pendingProjectReturnRoute = null) }
    }
}

data class AppUiState(
    val language: AppLanguage = AppLanguage.EN,
    val onboardingCompleted: Boolean = false,
    val articles: List<Article> = emptyList(),
    val quizzes: List<QuizTopic> = emptyList(),
    val projects: List<Project> = emptyList(),
    val news: List<NewsArticle> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val articleStatuses: Map<Int, ArticleStatus> = emptyMap(),
    val quizProgress: Map<String, QuizProgress> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
    val availableLanguages: List<AppLanguage> = AppLanguage.supported,
    val pendingScrollArticleId: Int? = null,
    val baseResetToken: Int = 0,
    val currentRoute: String? = null,
    val pendingRestoreRoute: String? = null,
    val newsFavorites: Set<String> = emptySet(),
    val projectFavorites: Set<String> = emptySet(),
    val baseReturn: AppViewModel.BaseReturnSnapshot? = null,
    val pendingNewsArticleId: String? = null,
    val pendingNewsLanguage: String? = null,
    val pendingNewsReturnRoute: String? = null,
    val pendingProjectId: String? = null,
    val pendingProjectLanguage: String? = null,
    val pendingProjectReturnRoute: String? = null
)
