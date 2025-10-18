package com.eb5.app.data.repositories

import android.content.res.AssetManager
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.QuizCatalog
import com.eb5.app.data.model.QuizTopic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

class QuizRepository(private val assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, QuizCatalog>()
    private val mutex = Mutex()
    private val listSerializerLegacy = ListSerializer(QuizTopic.serializer())

    suspend fun catalog(language: AppLanguage): QuizCatalog = mutex.withLock {
        cache.getOrPut(language.assetFolder) {
            runCatching { loadQuizzes(language.assetFolder) }
                .getOrElse { error ->
                    if (language != AppLanguage.EN) {
                        runCatching { loadQuizzes(AppLanguage.EN.assetFolder) }
                            .onFailure { error.printStackTrace() }
                            .getOrDefault(QuizCatalog(emptyList()))
                    } else {
                        error.printStackTrace()
                        QuizCatalog(emptyList())
                    }
                }
        }
    }

    private fun loadQuizzes(folder: String): QuizCatalog {
        val path = "content/$folder/eb5_quizzes.json"
        val raw = assets.open(path).bufferedReader().use { it.readText() }
        return runCatching {
            json.decodeFromString(QuizCatalog.serializer(), raw)
        }.getOrElse {
            val legacy = json.decodeFromString(listSerializerLegacy, raw)
            QuizCatalog(quizzes = legacy)
        }
    }
}
