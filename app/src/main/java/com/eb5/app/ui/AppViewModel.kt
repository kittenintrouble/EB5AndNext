package com.eb5.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eb5.app.BuildConfig
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Article
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.QuizProgress
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.preferences.UserPreferencesRepository
import com.eb5.app.data.repositories.ContentRepository
import com.eb5.app.data.repositories.ProjectRepository
import com.eb5.app.data.repositories.QuizRepository
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
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.snapshot.collect { snapshot ->
                _uiState.update { it.copy(isLoading = true, error = null) }
                runCatching {
                    val language = snapshot.language
                    LocaleManager.apply(language)
                    withContext(Dispatchers.IO) {
                        val articles = contentRepository.articles(language)
                        val quizzes = quizRepository.quizzes(language)
                        val projects = projectRepository.projects(language)
                        Triple(articles, quizzes, projects)
                    }
                }.onSuccess { (articles, quizzes, projects) ->
                    _uiState.update { current ->
                        current.copy(
                            language = snapshot.language,
                            onboardingCompleted = snapshot.onboardingCompleted,
                            articles = articles,
                            quizzes = quizzes,
                            projects = projects,
                            favorites = snapshot.favorites,
                            articleStatuses = snapshot.articleStatuses,
                            quizProgress = snapshot.quizProgress,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            language = snapshot.language,
                            onboardingCompleted = snapshot.onboardingCompleted,
                            favorites = snapshot.favorites,
                            articleStatuses = snapshot.articleStatuses,
                            quizProgress = snapshot.quizProgress,
                            isLoading = false,
                            error = throwable.message
                        )
                    }
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
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
}

data class AppUiState(
    val language: AppLanguage = AppLanguage.EN,
    val onboardingCompleted: Boolean = false,
    val articles: List<Article> = emptyList(),
    val quizzes: List<QuizTopic> = emptyList(),
    val projects: List<Project> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val articleStatuses: Map<Int, ArticleStatus> = emptyMap(),
    val quizProgress: Map<String, QuizProgress> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
    val availableLanguages: List<AppLanguage> = AppLanguage.supported
)
