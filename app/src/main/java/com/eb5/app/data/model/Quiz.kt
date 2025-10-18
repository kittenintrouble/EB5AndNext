package com.eb5.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizCatalog(
    val quizzes: List<QuizTopic>,
    val tracks: List<QuizTrack> = emptyList()
)

@Serializable
data class QuizTopic(
    val id: String,
    val termId: Int? = null,
    val title: String,
    val summary: String? = null,
    val category: String,
    val subcategory: String,
    val trackIds: List<String> = emptyList(),
    val goalTags: List<String> = emptyList(),
    val format: String = "Multi",
    val level: String = "M",
    val durationMinutes: Int = 5,
    val recommendedArticles: List<Int> = emptyList(),
    val requiredArticles: List<Int> = emptyList(),
    val tags: List<String> = emptyList(),
    val unlock: QuizUnlock? = null,
    val questions: List<QuizQuestion>
)

@Serializable
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

@Serializable
data class QuizUnlock(
    val minimumArticlesCompleted: Int? = null,
    val categoryRequirements: List<QuizCategoryRequirement> = emptyList(),
    val prerequisiteQuizIds: List<String> = emptyList()
)

@Serializable
data class QuizCategoryRequirement(
    val category: String,
    val minCompleted: Int
)

@Serializable
data class QuizTrack(
    val id: String,
    val title: String,
    val description: String,
    val quizIds: List<String>,
    val estimatedDurationMinutes: Int,
    val goalTag: String? = null,
    val category: String,
    val unlock: QuizTrackUnlock? = null,
    val certificate: QuizCertificateTemplate? = null
)

@Serializable
data class QuizTrackUnlock(
    val minimumArticlesCompleted: Int? = null,
    val categoryRequirements: List<QuizCategoryRequirement> = emptyList(),
    val prerequisiteQuizIds: List<String> = emptyList()
)

@Serializable
data class QuizCertificateTemplate(
    val accentColor: String = "#0B5FFF",
    val secondaryColor: String = "#EEF4FF",
    val backgroundGradient: List<String> = emptyList(),
    val signatureLabel: String = "",
    val signatureName: String = ""
)

@Serializable
data class QuizAttemptRecord(
    val id: String,
    val quizId: String,
    val trackId: String? = null,
    val score: Int,
    val totalQuestions: Int,
    val level: String,
    val durationMinutes: Int,
    val completedAt: Long
)

@Serializable
data class QuizInProgressState(
    val quizId: String,
    val currentIndex: Int,
    val score: Int,
    val startedAt: Long,
    val updatedAt: Long
)
