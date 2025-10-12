package com.eb5.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eb5.app.data.model.ArticleStatus
import androidx.compose.ui.res.stringResource
import com.eb5.app.R
import com.eb5.app.ui.navigation.AppDestination
import com.eb5.app.ui.navigation.BottomDestination
import com.eb5.app.ui.navigation.bottomDestinations
import com.eb5.app.ui.screens.ArticleDetailScreen
import com.eb5.app.ui.screens.BaseScreen
import com.eb5.app.ui.screens.HomeScreen
import com.eb5.app.ui.screens.OnboardingScreen
import com.eb5.app.ui.screens.ProgressScreen
import com.eb5.app.ui.screens.ProjectDetailScreen
import com.eb5.app.ui.screens.ProjectsScreen
import com.eb5.app.ui.screens.QuizResultScreen
import com.eb5.app.ui.screens.QuizRunnerScreen
import com.eb5.app.ui.screens.QuizzesScreen
import com.eb5.app.ui.screens.SettingsScreen
import com.eb5.app.ui.theme.EB5Theme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EB5App(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    EB5Theme(language = state.language) {
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.onboardingCompleted -> OnboardingScreen(
                language = state.language,
                availableLanguages = state.availableLanguages,
                onLanguageChanged = viewModel::setLanguage,
                onContinue = { viewModel.completeOnboarding(it) },
                articles = state.articles.takeIf { it.isNotEmpty() }
            )

            else -> {
                if (state.error != null) {
                    LaunchedEffect(state.error) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = state.error ?: "")
                        }
                    }
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route
                Scaffold(
                    topBar = {
                        val title = when (currentDestination) {
                            AppDestination.Home.route -> stringResource(R.string.title_home)
                            AppDestination.Base.route -> stringResource(R.string.title_base)
                            AppDestination.Quizzes.route -> stringResource(R.string.title_quizzes)
                            AppDestination.Progress.route -> stringResource(R.string.title_progress)
                            AppDestination.Projects.route -> stringResource(R.string.title_projects)
                            AppDestination.Settings.route -> stringResource(R.string.title_settings)
                            AppDestination.ArticleDetail.route -> stringResource(R.string.title_article_detail)
                            AppDestination.QuizRunner.route -> stringResource(R.string.title_quiz_runner)
                            AppDestination.QuizResult.route -> stringResource(R.string.title_quiz_result)
                            AppDestination.ProjectDetail.route -> stringResource(R.string.title_project_detail)
                            else -> stringResource(R.string.app_name)
                        }
                        TopAppBar(
                            title = { Text(title) },
                            actions = {
                                if (currentDestination !in listOf(AppDestination.Settings.route, AppDestination.Onboarding.route)) {
                                    IconButton(onClick = { navController.navigate(AppDestination.Settings.route) }) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.cd_open_settings))
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (currentDestination in bottomDestinations.map { it.route }) {
                            BottomNavigationBar(
                                navController = navController,
                                destinations = bottomDestinations,
                                currentRoute = currentDestination
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.Home.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        composable(AppDestination.Home.route) {
                            HomeScreen(
                                modifier = Modifier.fillMaxSize(),
                                articles = state.articles,
                                articleStatuses = state.articleStatuses,
                                favorites = state.favorites,
                                quizzes = state.quizzes,
                                quizProgress = state.quizProgress,
                                onExploreArticles = {
                                    navController.navigate(AppDestination.Base.route)
                                },
                                onOpenQuiz = { topic ->
                                    navController.navigate(AppDestination.QuizRunner.routeWithId(topic.id))
                                }
                            )
                        }
                        composable(AppDestination.Base.route) {
                            BaseScreen(
                                articles = state.articles,
                                articleStatuses = state.articleStatuses,
                                favorites = state.favorites,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onToggleStatus = viewModel::toggleArticleStatus,
                                onArticleSelected = { articleId ->
                                    navController.navigate(AppDestination.ArticleDetail.routeWithId(articleId))
                                }
                            )
                        }
                        composable(AppDestination.Quizzes.route) {
                            QuizzesScreen(
                                quizzes = state.quizzes,
                                quizProgress = state.quizProgress,
                                onQuizSelected = { topic ->
                                    navController.navigate(AppDestination.QuizRunner.routeWithId(topic.id))
                                }
                            )
                        }
                        composable(AppDestination.Progress.route) {
                            ProgressScreen(
                                articles = state.articles,
                                articleStatuses = state.articleStatuses,
                                favorites = state.favorites,
                                quizzes = state.quizzes,
                                quizProgress = state.quizProgress
                            )
                        }
                        composable(AppDestination.Projects.route) {
                            ProjectsScreen(
                                projects = state.projects,
                                onProjectSelected = { project ->
                                    navController.navigate(AppDestination.ProjectDetail.routeWithId(project.id))
                                }
                            )
                        }
                        composable(AppDestination.Settings.route) {
                            SettingsScreen(
                                language = state.language,
                                availableLanguages = state.availableLanguages,
                                onLanguageChanged = viewModel::setLanguage,
                                onClose = { navController.navigateUp() },
                                appVersion = state.versionName
                            )
                        }
                        composable(AppDestination.ArticleDetail.route) { backStackEntry ->
                            val articleId = backStackEntry.arguments?.getString("articleId")?.toIntOrNull()
                            val article = state.articles.firstOrNull { it.id == articleId }
                            if (article != null) {
                                ArticleDetailScreen(
                                    article = article,
                                    status = state.articleStatuses[article.id] ?: ArticleStatus.IN_PROGRESS,
                                    isFavorite = article.id in state.favorites,
                                    onToggleFavorite = { viewModel.toggleFavorite(article.id) },
                                    onToggleStatus = { viewModel.toggleArticleStatus(article.id) },
                                    onBack = { navController.navigateUp() }
                                )
                            }
                        }
                        composable(AppDestination.QuizRunner.route) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                            val topic = state.quizzes.firstOrNull { it.id == quizId }
                            if (topic != null) {
                                QuizRunnerScreen(
                                    topic = topic,
                                    onCompleted = { score ->
                                        viewModel.recordQuizResult(topic.id, score)
                                        navController.navigate(AppDestination.QuizResult.routeWithScore(topic.id, score))
                                    },
                                    onExit = { navController.navigateUp() }
                                )
                            }
                        }
                        composable(AppDestination.QuizResult.route) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                            val topic = state.quizzes.firstOrNull { it.id == quizId }
                            if (topic != null) {
                                QuizResultScreen(
                                    topic = topic,
                                    score = score,
                                    onRetake = {
                                        navController.navigate(AppDestination.QuizRunner.routeWithId(topic.id))
                                    },
                                    onBack = { navController.popBackStack(AppDestination.Quizzes.route, false) }
                                )
                            }
                        }
                        composable(AppDestination.ProjectDetail.route) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")
                            val project = state.projects.firstOrNull { it.id == projectId }
                            if (project != null) {
                                ProjectDetailScreen(project = project, onBack = { navController.navigateUp() })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    destinations: List<BottomDestination>,
    currentRoute: String?
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = destination.icon, contentDescription = null)
                },
                label = { Text(text = stringResource(destination.labelRes)) }
            )
        }
    }
}
