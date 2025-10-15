package com.eb5.app.data.repositories

import android.util.Log
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Financials
import com.eb5.app.data.model.JobCreation
import com.eb5.app.data.model.Project
import com.eb5.app.data.model.ProjectImage
import com.eb5.app.data.model.TeaInfo
import com.eb5.app.data.network.ProjectsApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProjectRepository(
    private val api: ProjectsApiService
) {

    private val cache = mutableMapOf<String, List<Project>>()
    private val cacheMutex = Mutex()

    suspend fun projects(
        language: AppLanguage,
        type: String? = null,
        status: String? = null,
        forceRefresh: Boolean = false
    ): List<Project> {
        val baseline = loadForLanguage(language, forceRefresh)
        return baseline.filter { project ->
            val typeMatches = type.isNullOrBlank() || project.type.equals(type, ignoreCase = true)
            val statusMatches = status.isNullOrBlank() || project.status.equals(status, ignoreCase = true)
            typeMatches && statusMatches
        }
    }

    suspend fun refresh(language: AppLanguage): List<Project> {
        val remoteResult = runCatching { fetchRemote(language) }
        val projects = remoteResult.getOrNull() ?: run {
            remoteResult.exceptionOrNull()?.let {
                Log.w(TAG, "Refresh failed for ${language.tag}", it)
            }
            emptyList()
        }
        cacheMutex.withLock {
            cache[language.assetFolder] = projects
        }
        return projects
    }

    suspend fun project(projectId: String, language: AppLanguage): Project? {
        val remote = runCatching { api.getProject(projectId, language.tag) }.getOrNull()
        if (remote != null) {
            val normalized = normalizeProject(remote, language)
            cacheMutex.withLock {
                val existing = cache[language.assetFolder].orEmpty()
                val updated = buildList {
                    add(normalized)
                    existing.filterNot { it.id == projectId }.forEach { add(it) }
                }
                cache[language.assetFolder] = updated
            }
            return normalized
        }
        return loadForLanguage(language, forceRefresh = false).firstOrNull { it.id == projectId }
    }

    private suspend fun loadForLanguage(language: AppLanguage, forceRefresh: Boolean): List<Project> {
        if (!forceRefresh) {
            cacheMutex.withLock {
                cache[language.assetFolder]?.let { return it }
            }
        }
        Log.d(TAG, "Attempting to load projects (forceRefresh=$forceRefresh) for ${language.tag}")
        val remoteResult = runCatching { fetchRemote(language) }
        val resolved = remoteResult.getOrElse { error ->
            Log.w(TAG, "Remote projects load failed for ${language.tag}", error)
            emptyList()
        }
        if (resolved.isEmpty()) {
            Log.w(TAG, "No projects available for ${language.tag}.")
        }
        cacheMutex.withLock {
            cache[language.assetFolder] = resolved
        }
        Log.d(TAG, "Loaded ${resolved.size} projects for ${language.tag}")
        return resolved
    }

    private suspend fun fetchRemote(language: AppLanguage): List<Project> {
        Log.d(TAG, "Fetching remote projects for ${language.tag}")
        val response = api.getProjects(
            language = language.tag,
            published = true,
            limit = 100
        )
        val normalized = response.all
            .map { normalizeProject(it, language) }
            .filter { it.published }
        if (normalized.isEmpty()) {
            Log.w(TAG, "Remote projects empty for ${language.tag}. Response total=${response.total}")
        }
        Log.d(TAG, "Remote returned ${normalized.size} projects for ${language.tag}")
        return normalized
    }

    private fun normalizeProject(project: Project, language: AppLanguage): Project {
        val normalizedImages = when {
            !project.images.isNullOrEmpty() -> project.images!!
            !project.image.isNullOrBlank() -> listOf(ProjectImage(url = project.image, alt = project.title))
            else -> emptyList()
        }
        val normalizedFinancials = project.financials ?: project.minInvestmentUsd?.let {
            Financials(
                totalProject = null,
                eb5Offering = null,
                minInvestment = it,
                eb5Investors = null,
                term = null,
                interestRate = null
            )
        }
        val normalizedTea = project.tea ?: project.teaStatus?.takeIf { status -> status.isNotBlank() }?.let {
            TeaInfo(type = it, designation = it)
        }
        val normalizedJobs = project.jobs ?: project.jobCreationModel?.takeIf { it.isNotBlank() }?.let {
            JobCreation(model = it)
        }
        val normalizedDescription = when {
            !project.shortDescription.isNullOrBlank() -> project.shortDescription!!
            !project.fullDescription.isNullOrBlank() -> project.fullDescription!!
            else -> ""
        }
        val normalizedFullDescription = project.fullDescription?.takeIf { it.isNotBlank() } ?: normalizedDescription
        val normalizedLocation = project.location?.takeIf { it.isNotBlank() }
        return project.copy(
            lang = project.lang ?: language.tag,
            type = project.type ?: project.category,
            images = normalizedImages,
            financials = normalizedFinancials,
            tea = normalizedTea,
            jobs = normalizedJobs,
            shortDescription = normalizedDescription,
            fullDescription = normalizedFullDescription,
            location = normalizedLocation
        )
    }

    companion object {
        private const val TAG = "ProjectRepository"
    }
}
