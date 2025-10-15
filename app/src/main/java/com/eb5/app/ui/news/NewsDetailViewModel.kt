package com.eb5.app.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.NewsArticle
import com.eb5.app.data.repositories.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsDetailUiState(
    val isLoading: Boolean = true,
    val article: NewsArticle? = null,
    val error: String? = null
)

class NewsDetailViewModel(
    private val newsRepository: NewsRepository,
    private val articleId: String,
    languageCode: String
) : ViewModel() {

    private val language: AppLanguage = languageCode
        .takeIf { it.isNotBlank() }
        ?.let { AppLanguage.fromTag(it) }
        ?: AppLanguage.EN

    private val _uiState = MutableStateFlow(NewsDetailUiState())
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    fun reload() {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                newsRepository.article(articleId, language)
            }.onSuccess { article ->
                _uiState.value = NewsDetailUiState(article = article, isLoading = false)
            }.onFailure { throwable ->
                _uiState.value = NewsDetailUiState(isLoading = false, error = throwable.message)
            }
        }
    }
}
