package com.eb5.app.data.model

enum class ArticleStatus { IN_PROGRESS, COMPLETED }

data class QuizProgress(
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val lastAttemptTimestamp: Long = 0L
)
