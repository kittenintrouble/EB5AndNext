package com.eb5.app.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Project
import com.eb5.app.data.repositories.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val error: String? = null
)

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val projectId: String,
    languageCode: String?
) : ViewModel() {

    private val language: AppLanguage = languageCode
        ?.takeIf { it.isNotBlank() }
        ?.let { AppLanguage.fromTag(it) }
        ?: AppLanguage.EN

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    fun reload() {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { projectRepository.project(projectId, language) }
                .onSuccess { project ->
                    if (project != null) {
                        _uiState.value = ProjectDetailUiState(project = project, isLoading = false)
                    } else {
                        _uiState.value = ProjectDetailUiState(
                            isLoading = false,
                            error = "Project details are unavailable."
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = ProjectDetailUiState(
                        isLoading = false,
                        error = throwable.message ?: "Project details are unavailable."
                    )
                }
        }
    }
}
