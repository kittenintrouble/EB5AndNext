package com.eb5.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector
import com.eb5.app.R

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object Home : AppDestination("home")
    data object Base : AppDestination("base")
    data object Quizzes : AppDestination("quizzes")
    data object Progress : AppDestination("progress")
    data object Projects : AppDestination("projects")
    data object Settings : AppDestination("settings")
    data object ArticleDetail : AppDestination("articleDetail/{articleId}") {
        fun routeWithId(articleId: Int) = "articleDetail/$articleId"
    }
    data object QuizRunner : AppDestination("quizRunner/{quizId}") {
        fun routeWithId(quizId: String) = "quizRunner/$quizId"
    }
    data object QuizResult : AppDestination("quizResult/{quizId}/{score}") {
        fun routeWithScore(quizId: String, score: Int) = "quizResult/$quizId/$score"
    }
    data object ProjectDetail : AppDestination("projectDetail/{projectId}") {
        fun routeWithId(projectId: String) = "projectDetail/$projectId"
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
    BottomDestination(AppDestination.Base, Icons.Filled.MenuBook, R.string.tab_base),
    BottomDestination(AppDestination.Quizzes, Icons.Filled.Quiz, R.string.tab_quizzes),
    BottomDestination(AppDestination.Progress, Icons.Filled.InsertChart, R.string.tab_progress),
    BottomDestination(AppDestination.Projects, Icons.Filled.Business, R.string.tab_projects)
)
