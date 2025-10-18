package com.eb5.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.QuizCatalog
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizAttemptRecord
import com.eb5.app.data.model.QuizInProgressState
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
    ) : java.io.Serializable

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

                val quizCatalog = withContext(Dispatchers.IO) {
                    runCatching { quizRepository.catalog(language) }
                }.getOrElse {
                    errors += "Quizzes could not be loaded."
                    previousState.quizCatalog
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
                        quizCatalog = quizCatalog,
                        projects = projects,
                        favorites = snapshot.favorites,
                        articleStatuses = snapshot.articleStatuses,
                        quizProgress = snapshot.quizProgress,
                        savedQuizzes = snapshot.savedQuizzes,
                        quizAttempts = snapshot.quizAttempts,
                        quizInProgress = snapshot.quizInProgressStates,
                        news = news,
                        isLoading = false,
                        error = errors.joinToString("\n").takeIf { message -> message.isNotBlank() },
                        pendingScrollArticleId = it.pendingScrollArticleId,
                        baseResetToken = it.baseResetToken,
                        currentRoute = it.currentRoute,
                        newsFavorites = snapshot.newsFavorites,
                        projectFavorites = snapshot.projectFavorites,
                        baseReturn = it.baseReturn,
                        pendingNewsArticleId = it.pendingNewsArticleId,
                        pendingNewsLanguage = it.pendingNewsLanguage,
                        pendingNewsReturnRoute = it.pendingNewsReturnRoute,
                        pendingProjectId = it.pendingProjectId,
                        pendingProjectLanguage = it.pendingProjectLanguage,
                        pendingProjectReturnRoute = it.pendingProjectReturnRoute,
                        pendingArticleReturnRoute = it.pendingArticleReturnRoute,
                        baseSelectedCategory = it.baseSelectedCategory,
                        baseSelectedSubcategory = it.baseSelectedSubcategory
                    )
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            if (_uiState.value.language == language) {
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
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
        val currentFavorites = _uiState.value.favorites
        val updatedFavorites = if (currentFavorites.contains(articleId)) {
            currentFavorites - articleId
        } else {
            currentFavorites + articleId
        }
        _uiState.update { it.copy(favorites = updatedFavorites) }
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(articleId)
        }
    }

    fun toggleArticleStatus(articleId: Int) {
        val current = _uiState.value.articleStatuses[articleId]
        val next = if (current == ArticleStatus.COMPLETED) ArticleStatus.IN_PROGRESS else ArticleStatus.COMPLETED
        _uiState.update { state ->
            state.copy(articleStatuses = state.articleStatuses + (articleId to next))
        }
        viewModelScope.launch {
            preferencesRepository.setArticleStatus(articleId, next)
        }
    }

    fun setArticleInProgress(articleId: Int) {
        _uiState.update { state ->
            state.copy(articleStatuses = state.articleStatuses + (articleId to ArticleStatus.IN_PROGRESS))
        }
        viewModelScope.launch {
            preferencesRepository.setArticleStatus(articleId, ArticleStatus.IN_PROGRESS)
        }
    }

    fun toggleQuizSaved(quizId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleQuizSaved(quizId)
        }
    }

    fun setQuizSaved(quizId: String, saved: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setQuizSaved(quizId, saved)
        }
    }

    fun recordQuizResult(quizId: String, score: Int) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            preferencesRepository.updateQuizResult(quizId, score, timestamp)
            val quiz = _uiState.value.quizCatalog.quizzes.firstOrNull { it.id == quizId }
            if (quiz != null) {
                val record = QuizAttemptRecord(
                    id = java.util.UUID.randomUUID().toString(),
                    quizId = quizId,
                    trackId = quiz.trackIds.firstOrNull(),
                    score = score,
                    totalQuestions = quiz.questions.size,
                    level = quiz.level,
                    durationMinutes = quiz.durationMinutes,
                    completedAt = timestamp
                )
                preferencesRepository.appendQuizAttempt(record)
            }
            preferencesRepository.updateQuizInProgress(quizId, null)
        }
    }

    fun saveQuizProgress(state: QuizInProgressState) {
        viewModelScope.launch {
            preferencesRepository.updateQuizInProgress(state.quizId, state)
        }
    }

    fun clearQuizProgress(quizId: String) {
        viewModelScope.launch {
            preferencesRepository.updateQuizInProgress(quizId, null)
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
                baseResetToken = it.baseResetToken + 1,
                baseSelectedCategory = null,
                baseSelectedSubcategory = null
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
        Log.d("AppViewModel", "recordBaseReturn category=$category subcategory=$subcategory index=$index offset=$offset")
        _uiState.update {
            it.copy(
                baseReturn = BaseReturnSnapshot(category, subcategory, index, offset, pending = true),
                pendingArticleReturnRoute = AppDestination.Base.route
            )
        }
    }

    fun activateBaseReturn() {
        _uiState.update { state ->
            val snapshot = state.baseReturn ?: return@update state
            Log.d("AppViewModel", "activateBaseReturn category=${snapshot.category} subcategory=${snapshot.subcategory} index=${snapshot.index} offset=${snapshot.offset}")
            state.copy(
                baseSelectedCategory = snapshot.category,
                baseSelectedSubcategory = snapshot.subcategory,
                baseReturn = snapshot.copy(pending = false)
            )
        }
    }

    fun clearBaseReturn() {
        _uiState.update { it.copy(baseReturn = null) }
    }

    fun restoreBaseFromSnapshot(snapshot: BaseReturnSnapshot) {
        _uiState.update { state ->
            state.copy(
                baseSelectedCategory = snapshot.category,
                baseSelectedSubcategory = snapshot.subcategory,
                baseReturn = snapshot.copy(pending = false)
            )
        }
    }

    fun setArticleReturnRoute(route: String?) {
        val snapshot = _uiState.value.baseReturn ?: return
        _uiState.update {
            it.copy(
                pendingArticleReturnRoute = route,
                baseSelectedCategory = snapshot.category,
                baseSelectedSubcategory = snapshot.subcategory
            )
        }
    }

    fun clearArticleReturnRoute() {
        _uiState.update { it.copy(pendingArticleReturnRoute = null) }
    }

    fun selectBaseCategory(category: String?) {
        _uiState.update { state ->
            val normalizedCategory = category?.takeIf { it.isNotBlank() }
            val preserveSubcategory = normalizedCategory != null && state.baseSelectedCategory == normalizedCategory
            state.copy(
                baseSelectedCategory = normalizedCategory,
                baseSelectedSubcategory = state.baseSelectedSubcategory.takeIf { preserveSubcategory }
            )
        }
    }

    fun selectBaseSubcategory(subcategory: String?) {
        _uiState.update { state ->
            val normalized = subcategory?.takeIf { it.isNotBlank() }
            state.copy(baseSelectedSubcategory = normalized)
        }
    }

    fun updateCurrentDestination(route: String?) {
        _uiState.update { it.copy(currentRoute = route) }
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

    fun resetHomeScroll() {
        _uiState.update { it.copy(homeResetToken = it.homeResetToken + 1) }
    }
}

data class AppUiState(
    val language: AppLanguage = AppLanguage.EN,
    val onboardingCompleted: Boolean = false,
    val articles: List<Article> = emptyList(),
    val quizCatalog: QuizCatalog = QuizCatalog(emptyList()),
    val projects: List<Project> = emptyList(),
    val news: List<NewsArticle> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val articleStatuses: Map<Int, ArticleStatus> = emptyMap(),
    val quizProgress: Map<String, QuizProgress> = emptyMap(),
    val savedQuizzes: Set<String> = emptySet(),
    val quizAttempts: List<QuizAttemptRecord> = emptyList(),
    val quizInProgress: Map<String, QuizInProgressState> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val availableLanguages: List<AppLanguage> = AppLanguage.supported,
    val pendingScrollArticleId: Int? = null,
    val baseResetToken: Int = 0,
    val currentRoute: String? = null,
    val newsFavorites: Set<String> = emptySet(),
    val projectFavorites: Set<String> = emptySet(),
    val baseReturn: AppViewModel.BaseReturnSnapshot? = null,
    val pendingNewsArticleId: String? = null,
    val pendingNewsLanguage: String? = null,
    val pendingNewsReturnRoute: String? = null,
    val pendingProjectId: String? = null,
    val pendingProjectLanguage: String? = null,
    val pendingProjectReturnRoute: String? = null,
    val pendingArticleReturnRoute: String? = null,
    val baseSelectedCategory: String? = null,
    val baseSelectedSubcategory: String? = null,
    val homeResetToken: Int = 0
)
