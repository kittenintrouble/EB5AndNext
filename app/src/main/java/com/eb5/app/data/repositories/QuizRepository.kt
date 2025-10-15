package com.eb5.app.data.repositories

import android.content.res.AssetManager
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.QuizTopic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class QuizRepository(private val assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, List<QuizTopic>>()
    private val mutex = Mutex()

    suspend fun quizzes(language: AppLanguage): List<QuizTopic> = mutex.withLock {
        cache.getOrPut(language.assetFolder) {
            runCatching { loadQuizzes(language.assetFolder) }
                .getOrElse { error ->
                    if (language != AppLanguage.EN) {
                        runCatching { loadQuizzes(AppLanguage.EN.assetFolder) }
                            .onFailure { error.printStackTrace() }
                            .getOrDefault(emptyList())
                    } else {
                        error.printStackTrace()
                        emptyList()
                    }
                }
        }
    }

    private fun loadQuizzes(folder: String): List<QuizTopic> {
        val path = "content/$folder/eb5_quizzes.json"
        return assets.open(path).bufferedReader().use { reader ->
            json.decodeFromString<List<QuizTopic>>(reader.readText())
        }
    }
}
