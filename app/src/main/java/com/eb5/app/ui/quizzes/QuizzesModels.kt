package com.eb5.app.ui.quizzes

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

enum class QuizzesTab { Tracks, All, Results }

enum class QuizSortOption {
    Recommended,
    InProgressFirst,
    Newest,
    Difficulty
}

data class QuizFilters(
    val goals: Set<String> = emptySet(),
    val duration: IntRange? = null,
    val formats: Set<String> = emptySet(),
    val levels: Set<String> = emptySet(),
    val sort: QuizSortOption = QuizSortOption.Recommended
)

data class QuizUi(
    val id: String,
    val title: String,
    val category: String,
    val format: String,
    val level: String,
    val durationMin: Int,
    val questionsCount: Int,
    val tags: List<String>,
    val bestScore: Int?,
    val passed: Boolean,
    val lastAttemptAt: Instant?,
    val inProgress: Boolean,
    val isSaved: Boolean
) {
    val levelLabel: String
        get() = when (level.uppercase()) {
            "L" -> "Beginner"
            "M" -> "Intermediate"
            "H" -> "Advanced"
            else -> level
        }

    val estimatedDurationLabel: String
        get() = "${durationMin} min · ${questionsCount} Q · $format"

    fun ctaLabel(): String = when {
        inProgress -> "Resume"
        bestScore != null -> "Retake"
        else -> "Start"
    }

    fun lastAttemptRelative(now: Instant = Instant.now()): String {
        val attempt = lastAttemptAt ?: return ""
        val days = ChronoUnit.DAYS.between(attempt, now)
        return when {
            days <= 0 -> "Last attempt: today"
            days == 1L -> "Last attempt: 1 day ago"
            days < 7 -> "Last attempt: $days days ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.systemDefault())
                "Last attempt: ${formatter.format(attempt)}"
            }
        }
    }
}

data class TrackUi(
    val id: String,
    val title: String,
    val description: String,
    val quizIds: List<String>,
    val estimatedDurationMin: Int,
    val completed: Int,
    val total: Int,
    val certificateAvailable: Boolean
) {
    val progressFraction: Float
        get() = if (total == 0) 0f else (completed.toFloat() / total)

    val durationLabel: String
        get() = "${estimatedDurationMin} min · $total quizzes"

    fun ctaLabel(): String = when {
        completed == 0 -> "Start track"
        completed < total -> "Continue"
        else -> "View track"
    }
}

data class AttemptSummary(
    val id: String,
    val title: String,
    val quizId: String?,
    val trackId: String?,
    val bestScore: Int,
    val totalQuestions: Int,
    val level: String,
    val durationMin: Int,
    val completedAt: Instant
) {
    fun bestScoreLabel(): String = "$bestScore/$totalQuestions"
    fun completedDateLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a")
            .withZone(ZoneId.systemDefault())
        return formatter.format(completedAt)
    }
}

data class CertificateUi(
    val trackId: String,
    val title: String,
    val completedAt: Instant
) {
    fun completedLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneId.systemDefault())
        return formatter.format(completedAt)
    }
}

data class QuizzesUiState(
    val tab: QuizzesTab = QuizzesTab.Tracks,
    val resumeQuizzes: List<QuizUi> = emptyList(),
    val tracks: List<TrackUi> = emptyList(),
    val allQuizzes: Map<String, List<QuizUi>> = emptyMap(),
    val filters: QuizFilters = QuizFilters(),
    val filterChips: QuizFilterUi = QuizFilterUi(),
    val results: List<AttemptSummary> = emptyList(),
    val certificates: List<CertificateUi> = emptyList(),
    val isFilterSheetVisible: Boolean = false
) {
    val hasResume: Boolean get() = resumeQuizzes.isNotEmpty()
    val hasTracks: Boolean get() = tracks.isNotEmpty()
    val hasResults: Boolean get() = results.isNotEmpty()
    val hasCertificates: Boolean get() = certificates.isNotEmpty()
}

data class QuizFilterUi(
    val goals: List<FilterChipState> = emptyList(),
    val durations: List<FilterChipState> = emptyList(),
    val formats: List<FilterChipState> = emptyList(),
    val levels: List<FilterChipState> = emptyList(),
    val sorts: List<FilterChipState> = emptyList()
)

data class FilterChipState(
    val id: String,
    val label: String,
    val selected: Boolean = false
)

fun estimateTrackDuration(quizzes: List<QuizUi>): Int {
    if (quizzes.isEmpty()) return 0
    return quizzes.sumOf { it.durationMin }
}

fun estimateReadingMinutes(text: String, wordsPerMinute: Int = 200): Int {
    val words = text.split("\\s+".toRegex()).count { it.isNotBlank() }
    return ceil(words / wordsPerMinute.toDouble()).toInt().coerceAtLeast(1)
}
