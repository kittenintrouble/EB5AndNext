package com.eb5.app.data.repositories

import android.content.res.AssetManager
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.Project
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class ProjectRepository(private val assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, List<Project>>()
    private val mutex = Mutex()

    suspend fun projects(language: AppLanguage): List<Project> = mutex.withLock {
        cache.getOrPut(language.assetFolder) {
            runCatching { loadProjects(language.assetFolder) }
                .getOrElse { error ->
                    if (language != AppLanguage.EN) {
                        runCatching { loadProjects(AppLanguage.EN.assetFolder) }
                            .onFailure { error.printStackTrace() }
                            .getOrDefault(emptyList())
                    } else {
                        error.printStackTrace()
                        emptyList()
                    }
                }
        }
    }

    private fun loadProjects(folder: String): List<Project> {
        val path = "content/$folder/projects.json"
        return assets.open(path).bufferedReader().use { reader ->
            json.decodeFromString<List<Project>>(reader.readText())
        }
    }
}
