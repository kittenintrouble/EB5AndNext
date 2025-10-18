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
import com.eb5.app.data.model.QuizAttemptRecord
import com.eb5.app.data.model.QuizInProgressState
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val ARTICLE_STATUS_PREFIX = "articleStatus:"
private const val FAVORITE_PREFIX = "favorite:"
private const val QUIZ_BEST_PREFIX = "quiz:bestScore:"
private const val QUIZ_LAST_ATTEMPT_PREFIX = "quiz:lastAttempt:"
private const val QUIZ_LAST_SCORE_PREFIX = "quiz:lastScore:"
private const val QUIZ_ATTEMPTS_KEY = "quiz:attempts"
private const val QUIZ_PROGRESS_STATE_KEY = "quiz:progress"
private const val QUIZ_SAVED_PREFIX = "quiz:saved:"

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    data class Snapshot(
        val language: AppLanguage = AppLanguage.EN,
        val onboardingCompleted: Boolean = false,
        val articleStatuses: Map<Int, ArticleStatus> = emptyMap(),
        val favorites: Set<Int> = emptySet(),
        val newsFavorites: Set<String> = emptySet(),
        val projectFavorites: Set<String> = emptySet(),
        val quizProgress: Map<String, QuizProgress> = emptyMap(),
        val savedQuizzes: Set<String> = emptySet(),
        val quizAttempts: List<QuizAttemptRecord> = emptyList(),
        val quizInProgressStates: Map<String, QuizInProgressState> = emptyMap()
    )

    private val languageKey = stringPreferencesKey("language")
    private val onboardingKey = booleanPreferencesKey("onboardingCompleted")
    private val newsFavoritePrefix = "newsFavorite:"
    private val projectFavoritePrefix = "projectFavorite:"
    private val quizAttemptsPrefKey = stringPreferencesKey(QUIZ_ATTEMPTS_KEY)
    private val quizProgressPrefKey = stringPreferencesKey(QUIZ_PROGRESS_STATE_KEY)
    private val json = Json { ignoreUnknownKeys = true }
    private val attemptListSerializer = ListSerializer(QuizAttemptRecord.serializer())
    private val progressListSerializer = ListSerializer(QuizInProgressState.serializer())

    val snapshot: Flow<Snapshot> = dataStore.data.map { preferences ->
        val language = AppLanguage.fromTag(preferences[languageKey])
        val onboarding = preferences[onboardingKey] ?: false
        val articleStatuses = mutableMapOf<Int, ArticleStatus>()
        val favorites = mutableSetOf<Int>()
        val newsFavorites = mutableSetOf<String>()
        val projectFavorites = mutableSetOf<String>()
        val quizProgress = mutableMapOf<String, QuizProgress>()
        val savedQuizzes = mutableSetOf<String>()

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
                key.name.startsWith(newsFavoritePrefix) -> {
                    val id = key.name.removePrefix(newsFavoritePrefix)
                    val isFavorite = value as? Boolean ?: false
                    if (isFavorite && id.isNotBlank()) {
                        newsFavorites.add(id)
                    }
                }
                key.name.startsWith(projectFavoritePrefix) -> {
                    val id = key.name.removePrefix(projectFavoritePrefix)
                    val isFavorite = value as? Boolean ?: false
                    if (isFavorite) projectFavorites.add(id)
                }
                key.name.startsWith(QUIZ_BEST_PREFIX) -> {
                    val id = key.name.removePrefix(QUIZ_BEST_PREFIX)
                    val bestScore = value as? Int ?: 0
                    val lastScore = preferences[intPreferencesKey(QUIZ_LAST_SCORE_PREFIX + id)] ?: 0
                    val lastAttempt = preferences[longPreferencesKey(QUIZ_LAST_ATTEMPT_PREFIX + id)] ?: 0L
                    quizProgress[id] = QuizProgress(bestScore, lastScore, lastAttempt)
                }
                key.name.startsWith(QUIZ_SAVED_PREFIX) -> {
                    val id = key.name.removePrefix(QUIZ_SAVED_PREFIX)
                    val saved = value as? Boolean ?: false
                    if (saved && id.isNotBlank()) {
                        savedQuizzes.add(id)
                    }
                }
            }
        }

        val attempts = preferences[quizAttemptsPrefKey]?.let { raw ->
            runCatching { json.decodeFromString(attemptListSerializer, raw) }.getOrDefault(emptyList())
        } ?: emptyList()
        val progressStatesList = preferences[quizProgressPrefKey]?.let { raw ->
            runCatching { json.decodeFromString(progressListSerializer, raw) }.getOrDefault(emptyList())
        } ?: emptyList()
        val progressStates = progressStatesList.associateBy { it.quizId }

        Snapshot(
            language = language,
            onboardingCompleted = onboarding,
            articleStatuses = articleStatuses,
            favorites = favorites,
            newsFavorites = newsFavorites,
            projectFavorites = projectFavorites,
            quizProgress = quizProgress,
            savedQuizzes = savedQuizzes,
            quizAttempts = attempts,
            quizInProgressStates = progressStates
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

    suspend fun toggleNewsFavorite(newsId: String) {
        dataStore.edit { preferences ->
            val key = booleanPreferencesKey(newsFavoritePrefix + newsId)
            val current = preferences[key] ?: false
            preferences[key] = !current
        }
    }

    suspend fun toggleProjectFavorite(projectId: String) {
        dataStore.edit { preferences ->
            val key = booleanPreferencesKey(projectFavoritePrefix + projectId)
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

    suspend fun toggleQuizSaved(quizId: String) {
        dataStore.edit { preferences ->
            val key = booleanPreferencesKey(QUIZ_SAVED_PREFIX + quizId)
            val current = preferences[key] ?: false
            preferences[key] = !current
        }
    }

    suspend fun setQuizSaved(quizId: String, saved: Boolean) {
        dataStore.edit { preferences ->
            val key = booleanPreferencesKey(QUIZ_SAVED_PREFIX + quizId)
            preferences[key] = saved
        }
    }

    suspend fun appendQuizAttempt(record: QuizAttemptRecord, maxEntries: Int = 50) {
        dataStore.edit { preferences ->
            val current = preferences[quizAttemptsPrefKey]?.let { raw ->
                runCatching { json.decodeFromString(attemptListSerializer, raw) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (current + record).takeLast(maxEntries)
            preferences[quizAttemptsPrefKey] = json.encodeToString(attemptListSerializer, updated)
        }
    }

    suspend fun updateQuizInProgress(quizId: String, state: QuizInProgressState?) {
        dataStore.edit { preferences ->
            val current = preferences[quizProgressPrefKey]?.let { raw ->
                runCatching { json.decodeFromString(progressListSerializer, raw) }.getOrDefault(emptyList())
            } ?: emptyList()
            val mutable = current.associateBy { it.quizId }.toMutableMap()
            if (state == null) {
                mutable.remove(quizId)
            } else {
                mutable[quizId] = state
            }
            if (mutable.isEmpty()) {
                preferences.remove(quizProgressPrefKey)
            } else {
                preferences[quizProgressPrefKey] = json.encodeToString(progressListSerializer, mutable.values.toList())
            }
        }
    }

}
