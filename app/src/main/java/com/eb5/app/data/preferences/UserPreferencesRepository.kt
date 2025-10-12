package com.eb5.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eb5.app.data.model.AppLanguage
import com.eb5.app.data.model.ArticleStatus
import com.eb5.app.data.model.QuizProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val ARTICLE_STATUS_PREFIX = "articleStatus:"
private const val FAVORITE_PREFIX = "favorite:"
private const val QUIZ_BEST_PREFIX = "quiz:bestScore:"
private const val QUIZ_LAST_ATTEMPT_PREFIX = "quiz:lastAttempt:"
private const val QUIZ_LAST_SCORE_PREFIX = "quiz:lastScore:"

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    data class Snapshot(
        val language: AppLanguage = AppLanguage.EN,
        val onboardingCompleted: Boolean = false,
        val articleStatuses: Map<Int, ArticleStatus> = emptyMap(),
        val favorites: Set<Int> = emptySet(),
        val quizProgress: Map<String, QuizProgress> = emptyMap()
    )

    private val languageKey = stringPreferencesKey("language")
    private val onboardingKey = booleanPreferencesKey("onboardingCompleted")

    val snapshot: Flow<Snapshot> = dataStore.data.map { preferences ->
        val language = AppLanguage.fromTag(preferences[languageKey])
        val onboarding = preferences[onboardingKey] ?: false
        val articleStatuses = mutableMapOf<Int, ArticleStatus>()
        val favorites = mutableSetOf<Int>()
        val quizProgress = mutableMapOf<String, QuizProgress>()

        preferences.asMap().forEach { (key, value) ->
            when {
                key.name.startsWith(ARTICLE_STATUS_PREFIX) -> {
                    val id = key.name.removePrefix(ARTICLE_STATUS_PREFIX).toIntOrNull() ?: return@forEach
                    val status = (value as? String)?.let {
                        runCatching { ArticleStatus.valueOf(it) }.getOrNull()
                    }
                    if (status != null) {
                        articleStatuses[id] = status
                    }
                }
                key.name.startsWith(FAVORITE_PREFIX) -> {
                    val id = key.name.removePrefix(FAVORITE_PREFIX).toIntOrNull() ?: return@forEach
                    val isFavorite = value as? Boolean ?: false
                    if (isFavorite) favorites.add(id)
                }
                key.name.startsWith(QUIZ_BEST_PREFIX) -> {
                    val id = key.name.removePrefix(QUIZ_BEST_PREFIX)
                    val bestScore = value as? Int ?: 0
                    val lastScore = preferences[intPreferencesKey(QUIZ_LAST_SCORE_PREFIX + id)] ?: 0
                    val lastAttempt = preferences[longPreferencesKey(QUIZ_LAST_ATTEMPT_PREFIX + id)] ?: 0L
                    quizProgress[id] = QuizProgress(bestScore, lastScore, lastAttempt)
                }
            }
        }

        Snapshot(
            language = language,
            onboardingCompleted = onboarding,
            articleStatuses = articleStatuses,
            favorites = favorites,
            quizProgress = quizProgress
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language.tag
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[onboardingKey] = completed
        }
    }

    suspend fun toggleFavorite(articleId: Int) {
        dataStore.edit { preferences ->
            val key = booleanPreferencesKey(FAVORITE_PREFIX + articleId)
            val current = preferences[key] ?: false
            preferences[key] = !current
        }
    }

    suspend fun setArticleStatus(articleId: Int, status: ArticleStatus) {
        dataStore.edit { preferences ->
            val key = stringPreferencesKey(ARTICLE_STATUS_PREFIX + articleId)
            preferences[key] = status.name
        }
    }

    suspend fun updateQuizResult(quizId: String, score: Int, timestamp: Long) {
        dataStore.edit { preferences ->
            val bestKey = intPreferencesKey(QUIZ_BEST_PREFIX + quizId)
            val lastKey = intPreferencesKey(QUIZ_LAST_SCORE_PREFIX + quizId)
            val timestampKey = longPreferencesKey(QUIZ_LAST_ATTEMPT_PREFIX + quizId)
            val currentBest = preferences[bestKey] ?: 0
            preferences[bestKey] = maxOf(currentBest, score)
            preferences[lastKey] = score
            preferences[timestampKey] = timestamp
        }
    }
}
