package com.eb5.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector
import com.eb5.app.R
import android.net.Uri

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object Home : AppDestination("home")
    data object Base : AppDestination("base")
    data object Quizzes : AppDestination("quizzes")
    data object QuizTrackDetail : AppDestination("quizzes/track/{trackId}") {
        fun routeWithId(trackId: String) = "quizzes/track/$trackId"
    }
    data object Progress : AppDestination("progress")
    data object Projects : AppDestination("projects")
    data object ArticleDetail : AppDestination("articleDetail/{articleId}") {
        fun routeWithId(articleId: Int) = "articleDetail/$articleId"
    }
    data object QuizRunner : AppDestination("quizRunner/{quizId}?trackId={trackId}") {
        fun routeWithId(quizId: String, trackId: String? = null): String {
            val base = "quizRunner/$quizId"
            val encodedTrack = trackId?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }
            return encodedTrack?.let { "$base?trackId=$it" } ?: base
        }
    }
    data object QuizResult : AppDestination("quizResult/{quizId}/{score}?trackId={trackId}") {
        fun routeWithScore(quizId: String, score: Int, trackId: String? = null): String {
            val base = "quizResult/$quizId/$score"
            val encodedTrack = trackId?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }
            return encodedTrack?.let { "$base?trackId=$it" } ?: base
        }
    }
    data object ProjectDetail : AppDestination("projectDetail/{projectId}?lang={lang}") {
        fun routeWithId(projectId: String, language: String? = null): String {
            val encodedId = Uri.encode(projectId)
            val encodedLang = language?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }
            return if (encodedLang != null) {
                "projectDetail/$encodedId?lang=$encodedLang"
            } else {
                "projectDetail/$encodedId"
            }
        }
    }
    data object NewsDetail : AppDestination("newsDetail/{articleId}?lang={lang}") {
        fun routeWithId(articleId: String, language: String? = null): String {
            val encodedId = Uri.encode(articleId)
            val encodedLang = language?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }
            return if (encodedLang != null) {
                "newsDetail/$encodedId?lang=$encodedLang"
            } else {
                "newsDetail/$encodedId"
            }
        }
    }
}

data class BottomDestination(
    val destination: AppDestination,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    val route: String = destination.route
}

val bottomDestinations = listOf(
    BottomDestination(AppDestination.Home, Icons.Filled.Home, R.string.tab_home),
    BottomDestination(AppDestination.Base, Icons.AutoMirrored.Filled.MenuBook, R.string.tab_base),
    BottomDestination(AppDestination.Quizzes, Icons.Filled.Quiz, R.string.tab_quizzes),
    BottomDestination(AppDestination.Progress, Icons.AutoMirrored.Filled.Article, R.string.tab_progress),
    BottomDestination(AppDestination.Projects, Icons.Filled.Business, R.string.tab_projects)
)
